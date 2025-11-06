package dental.core.users.modules.mail;

import dental.core.users.mail.models.EmailRequest;
import dental.core.users.mail.models.EmailType;
import dental.core.users.mail.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailTestController {

	private final EmailService emailService;

	@PostMapping("/test")
	public ResponseEntity<String> testEmail(@RequestParam String to) {
		try {
			// Ejemplo de email simple
			emailService.sendEmail(
					Arrays.asList(to),
					"Test Email",
					"<h1>Hola!</h1><p>Este es un email de prueba.</p>"
			);

			return ResponseEntity.ok("Email enviado correctamente");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al enviar email: " + e.getMessage());
		}
	}

	@PostMapping("/templated")
	public ResponseEntity<String> testTemplatedEmail(@RequestParam String to) {
		try {
			Map<String, Object> variables = new HashMap<>();
			variables.put("title", "Bienvenido a Nuestro Servicio");
			variables.put("userName", "Usuario Test");
			variables.put("message", "Gracias por registrarte en nuestra plataforma.");
			variables.put("actionUrl", "https://tu-sitio.com/activar");
			variables.put("actionText", "Activar Cuenta");
			variables.put("companyName", "Tu Empresa S.A.");

			EmailRequest request = new EmailRequest();
			request.setTo(Arrays.asList(to));
			request.setSubject("Bienvenido - Email con Template");
			request.setEmailType(EmailType.DEFAULT);
			request.setVariables(variables);

			emailService.sendTemplatedEmail(request);

			return ResponseEntity.ok("Email con template enviado correctamente");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al enviar email: " + e.getMessage());
		}
	}

	@PostMapping("/booking-confirmation")
	public ResponseEntity<String> testBookingConfirmation(@RequestParam String to) {
		try {
			Map<String, Object> variables = new HashMap<>();
			variables.put("title", "Confirmación de Reserva");
			variables.put("userName", "Juan Pérez");
			variables.put("message", "Tu reserva ha sido confirmada exitosamente.");
			variables.put("details", Arrays.asList(
					"Fecha: 15 de Diciembre, 2024",
					"Hora: 14:00",
					"Servicio: Consulta Médica",
					"Profesional: Dr. García"
			));
			variables.put("companyName", "Centro Médico");

			EmailRequest request = new EmailRequest();
			request.setTo(Arrays.asList(to));
			request.setSubject("Confirmación de Reserva");
			request.setEmailType(EmailType.DEFAULT);
			request.setVariables(variables);

			emailService.sendTemplatedEmail(request);

			return ResponseEntity.ok("Email de confirmación enviado correctamente");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error al enviar email: " + e.getMessage());
		}
	}
}
