package dental.core.users.modules.auth.controllers;

import dental.core.users.entities.UserEntity;
import dental.core.users.modules.auth.dto.UserDetailResponse;
import dental.core.users.modules.auth.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/users")
@RequiredArgsConstructor
@Tag(name = "Public Users", description = "Endpoints públicos para consumo entre microservicios")
public class PublicUserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID (público)")
    public ResponseEntity<UserDetailResponse> getById(@PathVariable Long id) {
        UserEntity user = userRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        UserDetailResponse dto = UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .picture(user.getPicture())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .phone(user.getPhone())
                .address(user.getAddress())
                .birthDate(user.getBirthDate())
                .isActive(user.getIsActive())
                .build();
        return ResponseEntity.ok(dto);
    }
}


