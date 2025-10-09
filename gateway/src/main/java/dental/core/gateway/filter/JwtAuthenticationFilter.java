package dental.core.gateway.filter;

import dental.core.gateway.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    // Rutas públicas que no requieren autenticación
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/users/auth/login",
            "/api/users/auth/register",
            "/api/users/auth/google",
            "/api/users/api/health",
            "/api/gateway",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing request for path: {}", path);

        // Permitir rutas públicas sin validación
        if (isPublicRoute(path)) {
            log.debug("Public route accessed: {}", path);
            return chain.filter(exchange);
        }

        // Verificar header de autorización
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        try {
            String token = authHeader.substring(7);
            
            // Validar el token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for path: {}", path);
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Extraer información del token y agregarla a los headers
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            log.debug("Valid token for user: {} with role: {}", email, role);

            // Agregar headers con información del usuario para los microservicios
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
            return onError(exchange, "Error processing authentication", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        log.error("Authentication error: {} - Status: {}", message, status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad para ejecutarse primero
    }
}

