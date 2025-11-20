package com.dentalCare.be_core.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "odontograms")
public class Odontogram extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "El paciente es requerido")
    private Patient patient;

    @Column(name = "dentition_type", nullable = false, length = 10)
    @NotBlank(message = "El tipo de dentición es requerido")
    @Size(max = 10, message = "El tipo de dentición no puede exceder 10 caracteres")
    private String dentitionType; // ADULT o CHILD

    @Column(name = "teeth_data", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Los datos de los dientes son requeridos")
    private String teethData; // JSON con todos los estados de los dientes
}

