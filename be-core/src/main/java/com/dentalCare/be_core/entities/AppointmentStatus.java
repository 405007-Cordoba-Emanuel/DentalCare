package com.dentalCare.be_core.entities;

/**
 * Enum que define los posibles estados de un turno o cita odontológica
 */
public enum AppointmentStatus {
    PROGRAMADO,   // Turno creado pero aún no confirmado
    CONFIRMADO,   // Turno confirmado por el dentista o el paciente
    COMPLETADO,   // Turno realizado exitosamente
    CANCELADO,    // Turno cancelado por el dentista o el paciente
    AUSENTE       // El paciente no se presentó al turno
}
