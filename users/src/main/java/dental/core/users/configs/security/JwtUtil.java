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

    // ==================== TOKEN GENERATION ====================

    public String generateToken(String id, String email, String firstName, String lastName, String picture, String role) {
        return generateToken(id, email, firstName, lastName, picture, role, null, null);
    }

    public String generateToken(String id, String email, String firstName, String lastName, String picture, String role, Long dentistId, Long patientId) {
        Map<String, Object> claims = buildBaseClaims(id, role, firstName, lastName, picture);
        addOptionalClaims(claims, dentistId, patientId);
        return createToken(claims, email);
    }

    private Map<String, Object> buildBaseClaims(String id, String role, String firstName, String lastName, String picture) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        claims.put("firstName", firstName);
        claims.put("lastName", lastName);
        claims.put("picture", picture);
        return claims;
    }

    private void addOptionalClaims(Map<String, Object> claims, Long dentistId, Long patientId) {
        if (dentistId != null) {
            claims.put("dentistId", dentistId);
        }
        if (patientId != null) {
            claims.put("patientId", patientId);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ==================== TOKEN EXTRACTION ====================

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Long extractDentistId(String token) {
        return extractOptionalIdClaim(token, "dentistId");
    }

    public Long extractPatientId(String token) {
        return extractOptionalIdClaim(token, "patientId");
    }

    private Long extractOptionalIdClaim(String token, String claimName) {
        Object claimValue = extractAllClaims(token).get(claimName);
        return claimValue != null ? ((Number) claimValue).longValue() : null;
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== TOKEN VALIDATION ====================

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
