package dental.core.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gateway")
public class ApiGatewayController {
    @GetMapping("")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("API Gateway activo y funcionando");
    }
}
