package cd.beapi.config;

import cd.beapi.security.exception.CustomAccessDeniedHandler;
import cd.beapi.security.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor

public class SecurityConfig {
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsConfig corsConfig;

    private record EndpointRule(HttpMethod method, String matcher) {}
    private record SecurityRule(String authority, List<EndpointRule> endpoints) {}
    private static final List<SecurityRule> SECURITY_RULES = List.of(
            new SecurityRule("USERS:READ", List.of(
                    new EndpointRule(HttpMethod.POST, "/api/users/search"),
                    new EndpointRule(HttpMethod.GET, "/api/users/{id}")
            )),
            new SecurityRule("USERS:WRITE", List.of(
                    new EndpointRule(HttpMethod.POST, "/api/users"),
                    new EndpointRule(HttpMethod.PUT, "/api/users/{id}")
            ))
    );
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .cors(c -> c.configurationSource(corsConfig))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/refresh", "/auth/login", "/auth/register", "/error", "/images/**").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("USERS:READ")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}

