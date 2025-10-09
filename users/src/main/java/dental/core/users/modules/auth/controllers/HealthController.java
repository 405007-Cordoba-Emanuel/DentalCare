package dental.core.users.modules.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "Check if health service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Health service is running");
    }
}
