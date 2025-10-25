package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.patient.CreatePatientFromUserRequest;
import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentResponseDto;
import com.dentalCare.be_core.services.MedicalHistoryService;
import com.dentalCare.be_core.services.PatientService;
import com.dentalCare.be_core.services.PrescriptionService;
import com.dentalCare.be_core.services.TreatmentService;
import com.dentalCare.be_core.services.AppointmentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/core/patient")
@Slf4j
@Validated
@Tag(name = "Patient", description = "API for patient management")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private MedicalHistoryService medicalHistoryService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * Crear paciente automáticamente desde registro de usuario
     * Endpoint público para ser llamado desde el microservicio de usuarios
     */
    @Operation(summary = "Crear paciente automáticamente desde registro")
    @PostMapping("/create-from-user")
    public ResponseEntity<PatientResponseDto> createPatientFromUser(
            @Parameter(description = "Datos del usuario") 
            @Valid @RequestBody CreatePatientFromUserRequest request) {
        try {
            PatientResponseDto createdPatient = patientService.createPatientFromUser(request);
            return ResponseEntity.ok(createdPatient);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating patient from user", e);
        }
    }

    @Operation(summary = "Obtener ID de paciente por userId")
    @GetMapping("/user-id/{userId}")
    public ResponseEntity<Long> getPatientIdByUserId(@PathVariable Long userId) {
        try {
            Long patientId = patientService.getPatientIdByUserId(userId);
            if (patientId == null) {
                throw new IllegalArgumentException("No patient found for userId: " + userId);
            }
            return ResponseEntity.ok(patientId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error getting patient ID by userId", e);
        }
    }

	// ------- Bloque Paciente -------
    /**
     * Consultar Paciente por ID
     * Obtiene la información completa de un paciente específico mediante su ID único.
     * Retorna todos los datos personales del paciente.
     */
	@Operation(summary = "Obtener paciente por ID")
    @GetMapping("/getById/{id}")
    public ResponseEntity<PatientResponseDto> getById(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        PatientResponseDto patient = patientService.searchById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Buscar Paciente por DNI
     * Permite buscar un paciente utilizando su número de documento de identidad.
     * Útil para verificar si un paciente ya existe en el sistema.
     */
	@Operation(summary = "Obtener paciente por DNI")
    @GetMapping("/dni/{dni}")
    public ResponseEntity<PatientResponseDto> getByLicenseNumber(
            @Parameter(description = "Patient's DNI", required = true)
            @PathVariable String dni) {

        PatientResponseDto patientResponseDto = patientService.searchByDni(dni);
        return ResponseEntity.ok(patientResponseDto);
    }

    /**
     * Listar Todos los Pacientes Activos
     * Retorna una lista completa de todos los pacientes que están activos en el sistema.
     * Incluye pacientes de todos los dentistas.
     */
	@Operation(summary = "Listar pacientes activos")
    @GetMapping("/getAllActive")
    public ResponseEntity<List<PatientResponseDto>> getAllActive() {
        List<PatientResponseDto> patientResponseDtoList = patientService.findAllActive();
        return ResponseEntity.ok(patientResponseDtoList);
    }

    /**
     * Modificar Datos del Paciente
     * Actualiza la información de un paciente existente (datos personales, contacto, etc.).
     * Valida que no se duplique DNI o email con otros pacientes.
     */
	@Operation(summary = "Actualizar paciente")
    @PutMapping("/update/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @Parameter(description = "Patient ID to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New data from the patient", required = true)
            @Valid @RequestBody PatientUpdateRequestDto patientUpdateRequestDto) {

        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientUpdateRequestDto);
        return ResponseEntity.ok(patientResponseDto);
    }

    /**
     * Eliminar Paciente
     * Elimina físicamente un paciente del sistema.
     * IMPORTANTE: Esto es una eliminación permanente, no es eliminación lógica.
     */
	@Operation(summary = "Eliminar paciente")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID to delete", required = true)
            @PathVariable Long id) {

        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Contar Pacientes Activos
     * Retorna el número total de pacientes que están activos en el sistema.
     * Útil para estadísticas y dashboards.
     */
	@Operation(summary = "Contar pacientes activos")
    @GetMapping("/countActive")
    public ResponseEntity<Long> countActivePatient() {
        long count = patientService.countActivePatient();
        return ResponseEntity.ok(count);
    }

	// ------- Bloque Recetas del Paciente -------
    /**
     * Ver Recetas del Paciente
     * El paciente puede consultar todas las recetas que le han emitido.
     * Las recetas se ordenan por fecha de emisión descendente (más recientes primero).
     */
	@Operation(summary = "Listar recetas del paciente")
    @GetMapping("/{id}/prescriptions")
    public ResponseEntity<List<PrescriptionResponseDto>> getPrescriptionsByPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByPatientId(id);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Ver Detalle de una Receta del Paciente
     * El paciente consulta el detalle completo de una receta específica que le emitieron.
     * Incluye medicamentos, observaciones y datos del dentista.
     */
	@Operation(summary = "Obtener receta por ID para el paciente")
    @GetMapping("/{id}/prescriptions/{prescriptionId}")
    public ResponseEntity<PrescriptionResponseDto> getPrescriptionByIdAndPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Prescription ID", required = true)
            @PathVariable Long prescriptionId) {
        PrescriptionResponseDto prescription = prescriptionService.getPrescriptionByIdAndPatientId(prescriptionId, id);
        return ResponseEntity.ok(prescription);
    }

    /**
     * Contar Recetas del Paciente
     * Retorna el número total de recetas activas que tiene el paciente.
     * Útil para estadísticas personales del paciente.
     */
	@Operation(summary = "Contar recetas del paciente")
    @GetMapping("/{id}/prescriptions/count")
    public ResponseEntity<Long> countPrescriptionsByPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        long count = prescriptionService.countPrescriptionsByPatientId(id);
        return ResponseEntity.ok(count);
    }

	// ------- Bloque Historia Clínica del Paciente -------
    /**
     * Ver Historia Clínica del Paciente
     * El paciente consulta su historia clínica completa (solo lectura).
     * Las entradas se ordenan por fecha descendente (más recientes primero).
     */
	@Operation(summary = "Listar historia clínica del paciente")
    @GetMapping("/{id}/medical-history")
    public ResponseEntity<List<MedicalHistoryResponseDto>> getMedicalHistoryByPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        List<MedicalHistoryResponseDto> history = medicalHistoryService.getMedicalHistoryByPatient(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Ver Detalle de Entrada de Historia Clínica
     * El paciente consulta el detalle completo de una entrada específica de su historia.
     * Incluye descripción, fecha, receta asociada e información del archivo adjunto si existe.
     */
	@Operation(summary = "Obtener entrada de historia clínica por ID para el paciente")
    @GetMapping("/{id}/medical-history/{entryId}")
    public ResponseEntity<MedicalHistoryResponseDto> getMedicalHistoryEntry(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId) {
        MedicalHistoryResponseDto entry = medicalHistoryService.getMedicalHistoryEntryByIdForPatient(entryId, id);
        return ResponseEntity.ok(entry);
    }

	// ------- Bloque Tratamientos del Paciente -------
    /**
     * Ver Tratamientos del Paciente
     * El paciente consulta todos los tratamientos que tiene activos.
     * Muestra nombre, estado, fechas y porcentaje de progreso de cada tratamiento.
     */
	@Operation(summary = "Listar tratamientos del paciente")
    @GetMapping("/{id}/treatments")
    public ResponseEntity<List<TreatmentResponseDto>> getTreatmentsByPatient(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        List<TreatmentResponseDto> treatments = treatmentService.getTreatmentsByPatient(id);
        return ResponseEntity.ok(treatments);
    }

    /**
     * Ver Detalle Completo de un Tratamiento
     * El paciente consulta el detalle de un tratamiento específico incluyendo TODAS las sesiones.
     * Muestra el progreso completo con todas las entradas de historia clínica relacionadas.
     * Permite ver fotos, recetas y descripciones de cada sesión del tratamiento.
     */
	@Operation(summary = "Obtener detalle de tratamiento para el paciente")
    @GetMapping("/{id}/treatments/{treatmentId}")
    public ResponseEntity<TreatmentDetailResponseDto> getTreatmentDetail(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId) {
        TreatmentDetailResponseDto treatment = treatmentService.getTreatmentDetailByIdForPatient(treatmentId, id);
        return ResponseEntity.ok(treatment);
    }

	// ------- Bloque Turnos del Paciente -------
    /**
     * Ver Todos los Turnos del Paciente
     * El paciente consulta todos sus turnos con el dentista.
     * Los turnos se ordenan por fecha/hora de inicio (más próximos primero).
     * Incluye turnos de todos los estados (programados, confirmados, completados, etc.).
     */
	@Operation(summary = "Listar turnos del paciente")
    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        List<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByPatientId(id);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Ver Próximos Turnos del Paciente
     * El paciente consulta únicamente sus turnos futuros (que aún no han ocurrido).
     * Útil para recordar las próximas citas programadas.
     */
	@Operation(summary = "Listar próximos turnos del paciente")
    @GetMapping("/{id}/appointments/upcoming")
    public ResponseEntity<List<AppointmentResponseDto>> getUpcomingAppointmentsByPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        List<AppointmentResponseDto> appointments = appointmentService.getUpcomingAppointmentsByPatientId(id);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Ver Detalle de un Turno
     * El paciente consulta la información completa de un turno específico.
     * Incluye fecha/hora, motivo, observaciones del dentista y datos del profesional.
     */
	@Operation(summary = "Obtener turno por ID para el paciente")
    @GetMapping("/{id}/appointments/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentByIdAndPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long appointmentId) {
        AppointmentResponseDto appointment = appointmentService.getAppointmentByIdAndPatientId(appointmentId, id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Contar Turnos del Paciente por Estado
     * El paciente puede ver cuántos turnos tiene en cada estado.
     * Estados: SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW.
     * Si no se especifica estado, cuenta todos los turnos activos.
     */
	@Operation(summary = "Contar turnos del paciente por estado")
    @GetMapping("/{id}/appointments/count")
    public ResponseEntity<Long> countAppointmentsByStatus(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Status filter (optional)", required = false)
            @RequestParam(required = false) com.dentalCare.be_core.entities.AppointmentStatus status) {
        long count;
        if (status != null) {
            count = appointmentService.countAppointmentsByPatientIdAndStatus(id, status);
        } else {
            count = appointmentService.getAppointmentsByPatientId(id).size();
        }
        return ResponseEntity.ok(count);
    }

}
