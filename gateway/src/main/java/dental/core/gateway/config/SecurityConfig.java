package dental.core.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configuración de seguridad del Gateway
     * - Deshabilitamos CSRF ya que es un API REST
     * - Permitimos rutas públicas (login, register, health checks)
     * - La validación JWT la hace el JwtAuthenticationFilter
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // Deshabilitar CSRF (no necesario para API REST stateless)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // Configurar autorización
                .authorizeExchange(exchanges -> exchanges
                        // Rutas públicas - Login y Register
                        .pathMatchers(
                                "/api/users/auth/login",
                                "/api/users/auth/register",
                                "/api/users/auth/google/**",
                                "/api/users/api/health"
                        ).permitAll()
                        
                        // Permitir requests OPTIONS para CORS preflight
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Ruta de health check del gateway
                        .pathMatchers("/api/gateway", "/api/gateway/**").permitAll()
                        
                        // Actuator endpoints
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Swagger UI (si lo usas)
                        .pathMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        
                        // Todas las demás rutas requieren autenticación
                        // La validación JWT se hace en el JwtAuthenticationFilter
                        .anyExchange().permitAll()
                );

        return http.build();
    }
}

