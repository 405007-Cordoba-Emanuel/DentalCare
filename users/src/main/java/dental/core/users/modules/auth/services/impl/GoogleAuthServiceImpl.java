package dental.core.users.modules.auth.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dental.core.users.configs.security.JwtUtil;
import dental.core.users.entities.Role;
import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.dto.AuthResponse;
import dental.core.users.modules.auth.dto.GoogleTokenRequest;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.GoogleAuthService;
import dental.core.users.modules.auth.services.GoogleTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final GoogleTokenService googleTokenService;

    @Value("${google.client.id}")
    private String googleClientId;
    
    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Override
    public AuthResponse loginOrRegister(GoogleTokenRequest request) {
        try {
            // Handle test token for development
            if ("test-token-development".equals(request.getIdToken())) {
                return handleTestToken();
            }
            
            // Validate Google ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            
            if (idToken == null) {
                log.error("Invalid Google ID token");
                throw new RuntimeException("Invalid ID token");
            }

            Payload payload = idToken.getPayload();

            // Extract user information
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");
            Boolean emailVerified = payload.getEmailVerified();

            // Validate required fields
            if (email == null || email.isEmpty()) {
                throw new RuntimeException("Email is required");
            }

            // Check if email is verified (optional but recommended)
            if (emailVerified != null && !emailVerified) {
                log.warn("Email not verified for user: {}", email);
            }

            // Find or create user
            UserEntity user = findOrCreateUser(email, name, picture);

            // Store Google tokens if provided in the request
            if (request.getAccessToken() != null && !request.getAccessToken().isEmpty()) {
                try {
                    String refreshToken = request.getRefreshToken();
                    Long expiresIn = request.getExpiresIn() != null ? request.getExpiresIn() : 3600L;
                    
                    googleTokenService.storeTokens(user, request.getAccessToken(), refreshToken, expiresIn);
                    log.info("Stored Google tokens for user: {}", email);
                } catch (Exception e) {
                    log.warn("Could not store Google tokens for user: {}. Error: {}", email, e.getMessage());
                }
            } else {
                log.info("No Google access token provided for user: {}. Google Calendar features may not work.", email);
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(email, user.getFirstName(), user.getLastName(), user.getPicture(), user.getRole().name());

            log.info("Successful authentication for user: {}", email);
            return AuthResponse.builder()
                    .token(token)
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .picture(user.getPicture())
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {
            log.error("Error during Google authentication: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
    
    private UserEntity findOrCreateUser(String email, String name, String picture) {
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            // Update user information if needed
            boolean updated = false;
            
            // Split name into firstName and lastName
            String[] nameParts = splitName(name);
            String firstName = nameParts[0];
            String lastName = nameParts[1];
            
            if (firstName != null && !firstName.equals(user.getFirstName())) {
                user.setFirstName(firstName);
                updated = true;
            }
            if (lastName != null && !lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                updated = true;
            }
            if (picture != null && !picture.equals(user.getPicture())) {
                user.setPicture(picture);
                updated = true;
            }
            
            if (updated) {
                user = userRepository.save(user);
                log.info("Updated user information for: {}", email);
            }
            
            return user;
        } else {
            // Split name into firstName and lastName
            String[] nameParts = splitName(name);
            String firstName = nameParts[0];
            String lastName = nameParts[1];
            
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .picture(picture)
                    .role(Role.PATIENT)
                    .isActive(true)
                    .build();
            
            UserEntity savedUser = userRepository.save(newUser);
            log.info("Created new user: {}", email);
            return savedUser;
        }
    }
    
    /**
     * Splits a full name into first name and last name
     * @param fullName The full name to split
     * @return Array with [firstName, lastName]
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"User", "Unknown"};
        }
        
        String[] parts = fullName.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        
        // If lastName is empty, use a default
        if (lastName.isEmpty()) {
            lastName = "User";
        }
        
        return new String[]{firstName, lastName};
    }
    
    private AuthResponse handleTestToken() {
        String testEmail = "test@example.com";
        String testFirstName = "Test User";
        String testLastName = "Test User";
        String testPicture = "https://example.com/test-picture.jpg";
        
        UserEntity user = findOrCreateUser(testEmail, testFirstName, testPicture);
        String token = jwtUtil.generateToken(testEmail, testFirstName, testLastName, testPicture, user.getRole().name());

        log.info("Test authentication successful for: {}", testEmail);
        return AuthResponse.builder()
                .token(token)
                .firstName(testFirstName)
                .lastName(testLastName)
                .email(testEmail)
                .picture(testPicture)
                .build();
    }

    @Override
    public AuthResponse handleOAuthCallback(String code) {
        try {
            // Intercambiar el código por tokens
            GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code);
            
            // Obtener información del usuario usando el access token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            
            // Extraer email del token ID si está disponible
            String email = null;
            if (tokenResponse.getIdToken() != null) {
                GoogleIdToken idToken = verifier.verify(tokenResponse.getIdToken());
                if (idToken != null) {
                    email = idToken.getPayload().getEmail();
                }
            }
            
            if (email == null) {
                throw new RuntimeException("No se pudo obtener el email del usuario");
            }
            
            // Buscar o crear usuario
            UserEntity user = findOrCreateUser(email, email, null);
            
            // Almacenar tokens
            googleTokenService.storeTokens(user, 
                tokenResponse.getAccessToken(), 
                tokenResponse.getRefreshToken(), 
                tokenResponse.getExpiresInSeconds());
            
            // Generar JWT
            String token = jwtUtil.generateToken(email, user.getFirstName(),user.getLastName(), user.getPicture(), user.getRole().name());
            
            log.info("OAuth callback successful for user: {}", email);
            return AuthResponse.builder()
                    .token(token)
                    .firstName(user.getFirstName())
                    .email(user.getEmail())
                    .picture(user.getPicture())
                    .build();
            
        } catch (Exception e) {
            log.error("Error en OAuth callback: {}", e.getMessage(), e);
            throw new RuntimeException("Error en OAuth callback: " + e.getMessage());
        }
    }
    
    private GoogleTokenResponse exchangeCodeForTokens(String code) throws Exception {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        
        HttpTransport transport = new NetHttpTransport();
        HttpRequestFactory requestFactory = transport.createRequestFactory();
        
        GenericUrl url = new GenericUrl(tokenUrl);
        HttpContent content = new UrlEncodedContent(
            Map.of(
                "client_id", googleClientId,
                "client_secret", googleClientSecret,
                "code", code,
                "grant_type", "authorization_code",
                "redirect_uri", "http://localhost:8080/api/auth/google/callback"
            )
        );
        
        HttpRequest request = requestFactory.buildPostRequest(url, content);
        request.setParser(new GsonFactory().createJsonObjectParser());
        HttpResponse response = request.execute();
        
        return response.parseAs(GoogleTokenResponse.class);
    }
    
}
