package cd.beapi.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    private static final String PERMISSION_SEPARATOR = ":";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    public Set<String> extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(authority -> !authority.contains(PERMISSION_SEPARATOR))
                .collect(Collectors.toSet());
    }

    public Set<String> extractPerm(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(authority -> authority.contains(PERMISSION_SEPARATOR))
                .collect(Collectors.toSet());
    }

    public String generateAccessToken(Authentication authentication) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authentication.getName())
                .claim("role", extractRole(authentication))
                .claim("permission", extractPerm(authentication))
                .claim("type", "access").issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authentication.getName())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key())
                .compact();
    }

    public long getAccessExpirationInSeconds() {
        return accessExpiration / 1000;
    }

    public long getRefreshExpirationInSeconds() {
        return refreshExpiration / 1000;
    }

}
