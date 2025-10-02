package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.services.MedicalHistoryService;
import com.dentalCare.be_core.services.PatientService;
import com.dentalCare.be_core.services.PrescriptionService;
import com.dentalCare.be_core.services.TreatmentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/patient")
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


    /**
     * Consultar Paciente por ID
     * Obtiene la información completa de un paciente específico mediante su ID único.
     * Retorna todos los datos personales del paciente.
     */
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
    @GetMapping("/countActive")
    public ResponseEntity<Long> countActivePatient() {
        long count = patientService.countActivePatient();
        return ResponseEntity.ok(count);
    }

    /**
     * Ver Recetas del Paciente
     * El paciente puede consultar todas las recetas que le han emitido.
     * Las recetas se ordenan por fecha de emisión descendente (más recientes primero).
     */
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
    @GetMapping("/{id}/prescriptions/count")
    public ResponseEntity<Long> countPrescriptionsByPatientId(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        long count = prescriptionService.countPrescriptionsByPatientId(id);
        return ResponseEntity.ok(count);
    }

    /**
     * Ver Historia Clínica del Paciente
     * El paciente consulta su historia clínica completa (solo lectura).
     * Las entradas se ordenan por fecha descendente (más recientes primero).
     */
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
    @GetMapping("/{id}/medical-history/{entryId}")
    public ResponseEntity<MedicalHistoryResponseDto> getMedicalHistoryEntry(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId) {
        MedicalHistoryResponseDto entry = medicalHistoryService.getMedicalHistoryEntryByIdForPatient(entryId, id);
        return ResponseEntity.ok(entry);
    }

    /**
     * Ver Tratamientos del Paciente
     * El paciente consulta todos los tratamientos que tiene activos.
     * Muestra nombre, estado, fechas y porcentaje de progreso de cada tratamiento.
     */
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
    @GetMapping("/{id}/treatments/{treatmentId}")
    public ResponseEntity<TreatmentDetailResponseDto> getTreatmentDetail(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId) {
        TreatmentDetailResponseDto treatment = treatmentService.getTreatmentDetailByIdForPatient(treatmentId, id);
        return ResponseEntity.ok(treatment);
    }

}
