package ru.hikeload.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.hikeload.config.JwtProperties;
import ru.hikeload.domain.UserAccount;
import ru.hikeload.repository.UserAccountRepository;
import ru.hikeload.service.NotFoundException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final UserAccountRepository userAccountRepository;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties, UserAccountRepository userAccountRepository) {
        this.jwtProperties = jwtProperties;
        this.userAccountRepository = userAccountRepository;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AppUserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getExpirationHours() * 3600L);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getUserId())
                .claim("displayName", userDetails.getDisplayName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(UserAccount user) {
        return generateToken(new AppUserDetails(user));
    }

    public Authentication parseAuthentication(String token) {
        Claims claims = parseClaims(token);
        String email = claims.getSubject();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        AppUserDetails details = new AppUserDetails(user);
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
