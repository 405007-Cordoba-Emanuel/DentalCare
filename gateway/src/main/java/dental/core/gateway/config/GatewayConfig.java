package dental.core.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class GatewayConfig {

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        return routeLocatorBuilder.routes()
                .route("user-service", r -> r.path("/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(userServiceUrl))
                        .build();
    }
    /*
    * Filtro CORS global para el Gateway
    */
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");  // Permitir todos los orígenes
        config.addAllowedMethod("*");  // Permitir todos los métodos (GET, POST, OPTIONS, etc.)
        config.addAllowedHeader("*");  // Permitir todos los encabezados
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
