package dental.core.users.configs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String id, String email, String firstName,String lastName , String picture, String role) {
        Map<String, Object> claims = new HashMap<>();
		claims.put("id", id);
        claims.put("role",role);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);
        claims.put("picture", picture);
        
        return createToken(claims, email);
    }

    public String generateToken(String id, String email, String firstName, String lastName, String picture, String role, Long dentistId, Long patientId) {
        Map<String, Object> claims = new HashMap<>();
		claims.put("id", id);
        claims.put("role", role);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);
        claims.put("picture", picture);
        if (dentistId != null) {
            claims.put("dentistId", dentistId);
        }
        if (patientId != null) {
            claims.put("patientId", patientId);
        }
        
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder().claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractDentistId(String token) {
        Object dentistId = extractAllClaims(token).get("dentistId");
        return dentistId != null ? ((Number) dentistId).longValue() : null;
    }

    public Long extractPatientId(String token) {
        Object patientId = extractAllClaims(token).get("patientId");
        return patientId != null ? ((Number) patientId).longValue() : null;
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
