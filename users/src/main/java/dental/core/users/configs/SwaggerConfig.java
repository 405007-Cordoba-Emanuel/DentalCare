package dental.core.users.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		Server devServer = new Server();
		devServer.setUrl("http://localhost:8082");
		devServer.setDescription("Server URL in Development environment");

		Contact contact = new Contact();
		contact.setEmail("tobias.moreno@example.com");
		contact.setName("Tobias Moreno");

		Info info = new Info()
				.title("SmartBooking API")
				.version("1.0")
				.contact(contact)
				.description("API para gestión de reservas");

		// Configuración de seguridad JWT
		SecurityScheme securityScheme = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description("Ingresa tu token JWT aquí");

		// Aplicar seguridad globalmente
		SecurityRequirement securityRequirement = new SecurityRequirement()
				.addList("Bearer Authentication");

		return new OpenAPI()
				.info(info)
				.servers(List.of(devServer))
				.components(new Components().addSecuritySchemes("Bearer Authentication", securityScheme))
				.addSecurityItem(securityRequirement);
	}

}
