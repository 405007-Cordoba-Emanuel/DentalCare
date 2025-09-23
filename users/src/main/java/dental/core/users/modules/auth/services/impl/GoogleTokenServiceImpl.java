package dental.core.users.modules.auth.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.repositories.UserRepository;
import dental.core.users.modules.auth.services.GoogleTokenService;
import dental.core.users.modules.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleTokenServiceImpl implements GoogleTokenService {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${google.client.id}")
    private String clientId;
    
    @Value("${google.client.secret}")
    private String clientSecret;

    @Override
    public String getValidAccessToken(String userEmail) {
        UserEntity user = userService.findUserByEmail(userEmail);
        
        // Verificar si el token actual es válido
        if (isTokenValid(userEmail)) {
            return user.getGoogleAccessToken();
        }
        
        // Si no es válido, renovarlo
        return refreshAccessToken(userEmail);
    }

    @Override
    public String refreshAccessToken(String userEmail) {
        UserEntity user = userService.findUserByEmail(userEmail);
        
        if (user.getGoogleRefreshToken() == null) {
            throw new RuntimeException("El usuario no tiene un refresh token válido");
        }
        
        try {
            // Crear credenciales con el refresh token
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(new NetHttpTransport())
                    .setJsonFactory(new GsonFactory())
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setRefreshToken(user.getGoogleRefreshToken());
            
            // Renovar el token
            boolean refreshed = credential.refreshToken();
            
            if (!refreshed) {
                throw new RuntimeException("No se pudo renovar el token de acceso");
            }
            
            // Obtener el nuevo token y su tiempo de expiración
            String newAccessToken = credential.getAccessToken();
            Long expiresIn = credential.getExpiresInSeconds();
            
            // Actualizar el usuario con el nuevo token
            user.setGoogleAccessToken(newAccessToken);
            if (expiresIn != null) {
                user.setGoogleTokenExpiry(LocalDateTime.now().plusSeconds(expiresIn));
            }
            
            userRepository.save(user);
            log.info("Token renovado exitosamente para usuario: {}", userEmail);
            
            return newAccessToken;
            
        } catch (Exception e) {
            log.error("Error al renovar el token para usuario: {}", userEmail, e);
            throw new RuntimeException("Error al renovar el token de acceso", e);
        }
    }

    @Override
    public void storeTokens(UserEntity user, String accessToken, String refreshToken, long expiresIn) {
        user.setGoogleAccessToken(accessToken);
        user.setGoogleRefreshToken(refreshToken);
        user.setGoogleTokenExpiry(LocalDateTime.now().plusSeconds(expiresIn));
        
        userService.saveUser(user);
        log.info("Tokens almacenados para usuario: {}", user);
    }

    @Override
    public boolean isTokenValid(String userEmail) {
        UserEntity user = userService.findUserByEmail(userEmail);

        if (user.getGoogleAccessToken() == null || user.getGoogleTokenExpiry() == null) {
            return false;
        }
        
        // Verificar si el token no ha expirado (con un margen de 5 minutos)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = user.getGoogleTokenExpiry();
        
        return now.isBefore(expiryTime.minus(5, ChronoUnit.MINUTES));
    }
}
