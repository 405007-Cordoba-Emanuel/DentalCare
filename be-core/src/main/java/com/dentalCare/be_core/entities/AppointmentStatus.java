package com.dentalCare.be_core.entities;

/**
 * Enum que define los posibles estados de un turno/cita
 */
public enum AppointmentStatus {
    SCHEDULED,    // Programado - Turno creado pero no confirmado
    CONFIRMED,    // Confirmado - Turno confirmado por el dentista
    COMPLETED,    // Completado - Turno realizado exitosamente
    CANCELLED,    // Cancelado - Turno cancelado por dentista o paciente
    NO_SHOW       // No se presentó - Paciente no asistió al turno
}
