package cd.beapi.config;

import cd.beapi.security.exception.CustomAccessDeniedHandler;
import cd.beapi.security.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor

public class SecurityConfig {
    private static final String CLIENT_PORTAL_AUTHORITY = "PORTAL:CLIENT";
    private static final String CLINIC_PORTAL_AUTHORITY = "PORTAL:CLINIC";

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsConfig corsConfig;

    private record EndpointRule(HttpMethod method, String matcher, String... authorities) {}

    private static final List<EndpointRule> ENDPOINT_RULES = List.of(
            new EndpointRule(HttpMethod.GET, "/clinic/dashboard/stats", "DASHBOARD:VIEW"),

            new EndpointRule(HttpMethod.GET, "/clinic/patients/export", "PATIENT:EXPORT"),
            new EndpointRule(HttpMethod.GET, "/clinic/patients/*/detail", "PATIENT:VIEW"),
            new EndpointRule(HttpMethod.GET, "/clinic/patients/*", "PATIENT:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/patients/search", "PATIENT:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/patients", "PATIENT:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/patients/*", "PATIENT:UPDATE"),
            new EndpointRule(HttpMethod.DELETE, "/clinic/patients/*", "PATIENT:DELETE"),

            new EndpointRule(HttpMethod.GET, "/clinic/staffs/dentist-options", "APPOINTMENT:VIEW"),
            new EndpointRule(HttpMethod.GET, "/clinic/staffs/*", "STAFF:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/staffs/search", "STAFF:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/staffs", "STAFF:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/staffs/*", "STAFF:UPDATE"),
            new EndpointRule(HttpMethod.PATCH, "/clinic/staffs/*/status", "STAFF:UPDATE"),

            new EndpointRule(HttpMethod.GET, "/clinic/examinations/**", "DENTIST", "EXAMINATION:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/examinations/search", "DENTIST", "EXAMINATION:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/examinations/procedures/search", "DENTIST", "EXAMINATION:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/examinations/medicines/search", "DENTIST", "EXAMINATION:VIEW"),
            new EndpointRule(HttpMethod.PATCH, "/clinic/examinations/*/**", "DENTIST", "EXAMINATION:UPDATE"),
            new EndpointRule(HttpMethod.POST, "/clinic/examinations/treatments", "DENTIST", "EXAMINATION:UPDATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/examinations/treatments/*", "DENTIST", "EXAMINATION:UPDATE"),

            new EndpointRule(HttpMethod.GET, "/clinic/appointments/available-slots", "APPOINTMENT:VIEW"),
            new EndpointRule(HttpMethod.GET, "/clinic/appointments/*", "APPOINTMENT:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/appointments/search", "APPOINTMENT:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/appointments", "APPOINTMENT:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/appointments/*", "APPOINTMENT:UPDATE"),
            new EndpointRule(HttpMethod.PATCH, "/clinic/appointments/*/**", "APPOINTMENT:UPDATE"),
            new EndpointRule(HttpMethod.DELETE, "/clinic/appointments/*", "APPOINTMENT:DELETE"),

            new EndpointRule(HttpMethod.GET, "/clinic/medicines/export", "MEDICINE:EXPORT"),
            new EndpointRule(HttpMethod.GET, "/clinic/medicines/*", "MEDICINE:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/medicines/search", "MEDICINE:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/medicines", "MEDICINE:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/medicines/*", "MEDICINE:UPDATE"),
            new EndpointRule(HttpMethod.DELETE, "/clinic/medicines/*", "MEDICINE:DELETE"),

            new EndpointRule(HttpMethod.GET, "/clinic/procedures/*", "PROCEDURE:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/procedures/search", "PROCEDURE:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/procedures", "PROCEDURE:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/procedures/*", "PROCEDURE:UPDATE"),
            new EndpointRule(HttpMethod.DELETE, "/clinic/procedures/*", "PROCEDURE:DELETE"),

            new EndpointRule(HttpMethod.GET, "/clinic/treatments/patient/*/prescriptions", "TREATMENT:VIEW"),
            new EndpointRule(HttpMethod.GET, "/clinic/treatments/patient/*", "TREATMENT:VIEW"),
            new EndpointRule(HttpMethod.GET, "/clinic/treatments/*", "TREATMENT:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/treatments", "TREATMENT:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/treatments/*", "TREATMENT:UPDATE"),

            new EndpointRule(HttpMethod.GET, "/clinic/procedure-categories/*", "PROCEDURE:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/procedure-categories/search", "PROCEDURE:VIEW"),
            new EndpointRule(HttpMethod.POST, "/clinic/procedure-categories", "PROCEDURE:CREATE"),
            new EndpointRule(HttpMethod.PUT, "/clinic/procedure-categories/*", "PROCEDURE:UPDATE"),
            new EndpointRule(HttpMethod.DELETE, "/clinic/procedure-categories/*", "PROCEDURE:DELETE"),

            new EndpointRule(HttpMethod.GET, "/clinic/roles", "ADMIN"),
            new EndpointRule(HttpMethod.GET, "/clinic/roles/*", "ADMIN"),
            new EndpointRule(HttpMethod.GET, "/clinic/permissions/*", "ADMIN"),
            new EndpointRule(HttpMethod.PUT, "/clinic/permissions", "ADMIN"),
            new EndpointRule(HttpMethod.POST, "/clinic/accounts/search", "ADMIN"),
            new EndpointRule(HttpMethod.PATCH, "/clinic/accounts/*/status", "ADMIN"),
            new EndpointRule(HttpMethod.PUT, "/clinic/accounts/*/role", "ADMIN"),
            new EndpointRule(HttpMethod.POST, "/clinic/accounts/*/reset-password", "ADMIN"),
            new EndpointRule(HttpMethod.DELETE, "/clinic/accounts/*", "ADMIN")
    );
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .cors(c -> c.configurationSource(corsConfig))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/auth/refresh",
                            "/auth/client/refresh",
                            "/auth/clinic/refresh",
                            "/auth/client/login",
                            "/auth/clinic/login",
                            "/auth/register",
                            "/error",
                            "/images/**"
                    ).permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/public/booking/**").permitAll();
                    auth.requestMatchers("/client/**").hasAuthority(CLIENT_PORTAL_AUTHORITY);
                    ENDPOINT_RULES.forEach(rule ->
                            auth.requestMatchers(rule.method(), rule.matcher())
                                    .access((authentication, _) ->
                                            hasAllAuthorities(authentication, clinicAuthorities(rule))));
                    auth.requestMatchers("/clinic/**").hasAuthority(CLINIC_PORTAL_AUTHORITY);
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }

    private static AuthorizationDecision hasAllAuthorities(Supplier<? extends Authentication> authentication,
                                                           String... requiredAuthorities) {
        Authentication currentAuthentication = authentication.get();
        if (currentAuthentication == null || !currentAuthentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        Set<String> authorities = currentAuthentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean granted = Arrays.stream(requiredAuthorities).allMatch(authorities::contains);
        return new AuthorizationDecision(granted);
    }

    private static String[] clinicAuthorities(EndpointRule rule) {
        String[] requiredAuthorities = new String[rule.authorities().length + 1];
        requiredAuthorities[0] = CLINIC_PORTAL_AUTHORITY;
        System.arraycopy(rule.authorities(), 0, requiredAuthorities, 1, rule.authorities().length);
        return requiredAuthorities;
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
