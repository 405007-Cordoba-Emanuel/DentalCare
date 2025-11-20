package com.dentalCare.be_core.dtos.request.odontogram;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OdontogramRequestDto {

    @NotNull(message = "El ID del paciente es requerido")
    private Long patientId;

    @NotBlank(message = "El tipo de dentición es requerido")
    @Pattern(regexp = "^(adult|child)$", message = "El tipo de dentición debe ser 'adult' o 'child'")
    @Size(max = 10, message = "El tipo de dentición no puede exceder 10 caracteres")
    private String dentitionType;

    @NotBlank(message = "Los datos de los dientes son requeridos")
    private String teethData; // JSON string con todos los estados de los dientes
}

