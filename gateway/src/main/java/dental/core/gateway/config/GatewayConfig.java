package dental.core.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Value("${users.service.url}")
    private String usersServiceUrl;

    @Value("${core.service.url}")
    private String coreServiceUrl;

    /**
     * Configura las rutas del Gateway
     * - /api/users/** -> users-service:8081
     * - /api/core/** -> be-core-service:8082
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ruta para users-service (login, register, etc.)
                // /api/users/auth/login -> stripPrefix(2) -> /auth/login -> users:8081/api/auth/login
                .route("users-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .stripPrefix(2) // Elimina /api/users del path
                                .rewritePath("/(?<segment>.*)", "/api/${segment}")
                                .addRequestHeader("X-Gateway", "dental-gateway")
                        )
                        .uri(usersServiceUrl)
                )
                // Ruta para be-core-service (lógica de negocio)
                .route("core-service", r -> r
                        .path("/api/core/**")
                        .filters(f -> f
                                .stripPrefix(2) // Elimina /api/core del path
                                .addRequestHeader("X-Gateway", "dental-gateway")
                        )
                        .uri(coreServiceUrl)
                )
                .build();
    }

    /**
     * Configuración CORS para permitir requests desde Angular
     * Esta es la ÚNICA configuración CORS - no configurar en microservicios
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir origen de Angular
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        
        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales
        config.setAllowCredentials(true);
        
        // Headers expuestos
        config.setExposedHeaders(Arrays.asList("Authorization", "X-User-Email", "X-User-Role"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
