package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.request.prescription.PrescriptionRequestDto;
import com.dentalCare.be_core.dtos.request.treatment.TreatmentRequestDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistResponseDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistPatientsResponseDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.services.DentistService;
import com.dentalCare.be_core.services.MedicalHistoryService;
import com.dentalCare.be_core.services.PrescriptionPdfService;
import com.dentalCare.be_core.services.PrescriptionService;
import com.dentalCare.be_core.services.TreatmentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/dentist")
@Slf4j
@Validated
@Tag(name = "Dentist", description = "API for dentist management")
public class DentistController {

    @Autowired
    private DentistService dentistService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private MedicalHistoryService medicalHistoryService;

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private PrescriptionPdfService prescriptionPdfService;

    /**
     * Alta de Dentista
     * Crea un nuevo dentista en el sistema con todos sus datos personales y profesionales.
     * Valida que no exista otro dentista con la misma matrícula o email.
     */
    @PostMapping("/create")
    public ResponseEntity<DentistResponseDto> postDentist(@RequestBody DentistRequestDto dentistRequestDto) {
        try {
            DentistResponseDto createdDentist = dentistService.createDentist(dentistRequestDto);
            return ResponseEntity.ok(createdDentist);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating dentist", e);
        }
    }

    /**
     * Consultar Dentista por ID
     * Obtiene la información completa de un dentista específico mediante su ID único.
     * Retorna todos los datos personales y profesionales del dentista.
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<DentistResponseDto> getById(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistResponseDto dentist = dentistService.searchById(id);
        return ResponseEntity.ok(dentist);
    }

    /**
     * Buscar Dentista por Matrícula
     * Permite buscar un dentista utilizando su número de matrícula profesional.
     * Útil para verificar la existencia y validez de una matrícula.
     */
    @GetMapping("/licenseNumber/{licenseNumber}")
    public ResponseEntity<DentistResponseDto> getByLicenseNumber(
            @Parameter(description = "Dentist's License Number", required = true)
            @PathVariable String licenseNumber) {

        DentistResponseDto dentistResponseDto = dentistService.searchByLicenseNumber(licenseNumber);
        return ResponseEntity.ok(dentistResponseDto);
    }

    /**
     * Listar Todos los Dentistas Activos
     * Retorna una lista completa de todos los dentistas que están activos en el sistema.
     * Los dentistas se ordenan alfabéticamente por nombre y apellido.
     */
    @GetMapping("/getAllActive")
    public ResponseEntity<List<DentistResponseDto>> getAllActive() {
        List<DentistResponseDto> dentistResponseDtoList = dentistService.findAllActive();
        return ResponseEntity.ok(dentistResponseDtoList);
    }

    /**
     * Buscar Dentistas por Especialidad
     * Filtra y retorna todos los dentistas activos que tienen una especialidad específica.
     * Por ejemplo: Ortodoncia, Endodoncia, Periodoncia, etc.
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DentistResponseDto>> getBySpecialty(
            @Parameter(description = "Specialty to look for", required = true)
            @PathVariable String specialty) {

        List<DentistResponseDto> dentistResponseDtoList = dentistService.searchBySpecialty(specialty);
        return ResponseEntity.ok(dentistResponseDtoList);
    }

    /**
     * Modificar Datos de Dentista
     * Actualiza la información de un dentista existente (datos personales, contacto, especialidad, etc.).
     * Valida que no se duplique matrícula o email con otros dentistas.
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<DentistResponseDto> updateDentist(
            @Parameter(description = "Dentist ID to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New data from the dentist", required = true)
            @Valid @RequestBody DentistUpdateRequestDto dentistUpdateRequestDto) {

        DentistResponseDto dentistResponseDto = dentistService.updateDentist(id, dentistUpdateRequestDto);
        return ResponseEntity.ok(dentistResponseDto);
    }

    /**
     * Eliminar Dentista
     * Elimina físicamente un dentista del sistema.
     * IMPORTANTE: Esto es una eliminación permanente, no es eliminación lógica.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDentist(
            @Parameter(description = "Dentist ID to delete", required = true)
            @PathVariable Long id) {

        dentistService.deleteDentist(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Contar Dentistas Activos
     * Retorna el número total de dentistas que están activos en el sistema.
     * Útil para estadísticas y dashboards.
     */
    @GetMapping("/countActive")
    public ResponseEntity<Long> countActiveDentist() {
        long count = dentistService.countActiveDentist();
        return ResponseEntity.ok(count);
    }

    /**
     * Ver Todos los Pacientes de un Dentista
     * Retorna la lista completa de pacientes asignados a un dentista específico.
     * Incluye tanto pacientes activos como inactivos.
     */
    @GetMapping("/{id}/patients")
    public ResponseEntity<DentistPatientsResponseDto> getPatientsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistPatientsResponseDto response = dentistService.getPatientsByDentistId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Ver Pacientes Activos de un Dentista
     * Retorna solamente los pacientes que están activos de un dentista específico.
     * Filtra los pacientes inactivos o dados de baja.
     */
    @GetMapping("/{id}/patients/active")
    public ResponseEntity<DentistPatientsResponseDto> getActivePatientsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistPatientsResponseDto response = dentistService.getActivePatientsByDentistId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Alta de Paciente por el Dentista
     * El dentista da de alta un nuevo paciente en el sistema, asignándolo automáticamente a él.
     * El paciente queda vinculado al dentista desde su creación.
     */
    @PostMapping("/{id}/patients")
    public ResponseEntity<PatientResponseDto> createPatientForDentist(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDto patientRequestDto) {
        try {
            PatientResponseDto createdPatient = dentistService.createPatientForDentist(id, patientRequestDto);
            return ResponseEntity.ok(createdPatient);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating patient for dentist", e);
        }
    }

    /**
     * Emitir Receta Médica
     * El dentista crea una receta/prescripción médica para un paciente específico.
     * Incluye medicamentos, observaciones y fecha de emisión.
     */
    @PostMapping("/{id}/prescriptions")
    public ResponseEntity<PrescriptionResponseDto> createPrescriptionForDentist(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionRequestDto prescriptionRequestDto) {
        try {
            PrescriptionResponseDto createdPrescription = prescriptionService.createPrescriptionForDentist(id, prescriptionRequestDto);
            return ResponseEntity.ok(createdPrescription);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating prescription for dentist", e);
        }
    }

    /**
     * Ver Todas las Recetas del Dentista
     * Retorna todas las recetas emitidas por un dentista específico.
     * Las recetas se ordenan por fecha de emisión descendente (más recientes primero).
     */
    @GetMapping("/{id}/prescriptions")
    public ResponseEntity<List<PrescriptionResponseDto>> getPrescriptionsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByDentistId(id);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Ver Recetas de un Paciente Específico
     * Retorna todas las recetas que el dentista ha emitido para un paciente en particular.
     * Útil para ver el historial de prescripciones de un paciente.
     */
    @GetMapping("/{id}/prescriptions/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponseDto>> getPrescriptionsByDentistIdAndPatientId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId) {
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByDentistIdAndPatientId(id, patientId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Ver Detalle de una Receta
     * Obtiene la información completa de una receta específica emitida por el dentista.
     * Incluye datos del paciente, medicamentos, observaciones y fecha.
     */
    @GetMapping("/{id}/prescriptions/{prescriptionId}")
    public ResponseEntity<PrescriptionResponseDto> getPrescriptionByIdAndDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Prescription ID", required = true)
            @PathVariable Long prescriptionId) {
        PrescriptionResponseDto prescription = prescriptionService.getPrescriptionByIdAndDentistId(prescriptionId, id);
        return ResponseEntity.ok(prescription);
    }

    /**
     * Modificar Receta Existente
     * Actualiza los datos de una receta previamente emitida (medicamentos, observaciones, etc.).
     * Solo el dentista que emitió la receta puede modificarla.
     */
    @PutMapping("/{id}/prescriptions/{prescriptionId}")
    public ResponseEntity<PrescriptionResponseDto> updatePrescription(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Prescription ID", required = true)
            @PathVariable Long prescriptionId,
            @Valid @RequestBody PrescriptionRequestDto prescriptionRequestDto) {
        try {
            PrescriptionResponseDto updatedPrescription = prescriptionService.updatePrescription(prescriptionId, id, prescriptionRequestDto);
            return ResponseEntity.ok(updatedPrescription);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating prescription", e);
        }
    }

    /**
     * Eliminar Receta
     * Elimina físicamente una receta del sistema.
     * IMPORTANTE: Esta es una eliminación permanente.
     */
    @DeleteMapping("/{id}/prescriptions/{prescriptionId}")
    public ResponseEntity<Void> deletePrescription(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Prescription ID", required = true)
            @PathVariable Long prescriptionId) {
        prescriptionService.deletePrescription(prescriptionId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Contar Recetas del Dentista
     * Retorna el número total de recetas activas emitidas por el dentista.
     * Útil para estadísticas y reportes.
     */
    @GetMapping("/{id}/prescriptions/count")
    public ResponseEntity<Long> countPrescriptionsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        long count = prescriptionService.countPrescriptionsByDentistId(id);
        return ResponseEntity.ok(count);
    }

    /**
     * Descargar Receta en Formato PDF
     * Genera y descarga una receta médica en formato PDF profesional listo para imprimir.
     * Incluye membrete del consultorio, datos del paciente, medicamentos y espacio para firma.
     * El dentista puede imprimir y firmar la receta para entregar al paciente.
     */
    @GetMapping("/{id}/prescriptions/{prescriptionId}/download-pdf")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Prescription ID", required = true)
            @PathVariable Long prescriptionId) {
        try {
            com.dentalCare.be_core.entities.Prescription prescription = 
                prescriptionService.getPrescriptionEntityById(prescriptionId, id);
            
            byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                    .filename("Receta: " + prescriptionId + " " + prescription.getPatient().getFirstName() + " - " + prescription.getPatient().getLastName() + ".pdf")
                    .build()
            );
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error generating prescription PDF", e);
        }
    }

    /**
     * Crear Entrada en Historia Clínica
     * El dentista registra una nueva entrada en la historia clínica del paciente.
     * Incluye descripción, fecha, opcionalmente receta y archivo adjunto (foto/PDF).
     * Los campos se envían como form-data individual compatible con Swagger.
     */
    @PostMapping(value = "/{id}/patients/{patientId}/medical-history", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalHistoryResponseDto> createMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId,
            @Parameter(description = "Entry date (format: yyyy-MM-dd)", required = true)
            @RequestParam("entryDate") String entryDate,
            @Parameter(description = "Description", required = true)
            @RequestParam("description") String description,
            @Parameter(description = "Prescription ID", required = false)
            @RequestParam(value = "prescriptionId", required = false) Long prescriptionId,
            @Parameter(description = "Treatment ID", required = false)
            @RequestParam(value = "treatmentId", required = false) Long treatmentId,
            @Parameter(description = "File attachment (JPG, PNG, PDF)", required = false)
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            MedicalHistoryRequestDto requestDto = new MedicalHistoryRequestDto();
            requestDto.setPatientId(patientId);
            requestDto.setEntryDate(java.time.LocalDate.parse(entryDate));
            requestDto.setDescription(description);
            requestDto.setPrescriptionId(prescriptionId);
            requestDto.setTreatmentId(treatmentId);
            
            MedicalHistoryResponseDto response = medicalHistoryService.createMedicalHistoryEntry(id, requestDto, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating medical history entry", e);
        }
    }

    /**
     * Ver Historia Clínica Completa de un Paciente
     * Retorna todas las entradas de la historia clínica de un paciente específico.
     * Las entradas se ordenan por fecha descendente (más recientes primero).
     */
    @GetMapping("/{id}/patients/{patientId}/medical-history")
    public ResponseEntity<List<MedicalHistoryResponseDto>> getMedicalHistoryByPatient(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId) {
        List<MedicalHistoryResponseDto> history = medicalHistoryService.getMedicalHistoryByDentistAndPatient(id, patientId);
        return ResponseEntity.ok(history);
    }

    /**
     * Ver Detalle de Entrada de Historia Clínica
     * Obtiene la información completa de una entrada específica de la historia clínica.
     * Incluye descripción, fecha, receta asociada e información del archivo adjunto si existe.
     */
    @GetMapping("/{id}/medical-history/{entryId}")
    public ResponseEntity<MedicalHistoryResponseDto> getMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId) {
        MedicalHistoryResponseDto entry = medicalHistoryService.getMedicalHistoryEntryById(entryId, id);
        return ResponseEntity.ok(entry);
    }

    /**
     * Modificar Entrada de Historia Clínica
     * Actualiza una entrada existente de la historia clínica (descripción, fecha, archivo, etc.).
     * Si se envía un nuevo archivo, reemplaza el anterior. Los campos se envían individualmente.
     */
    @PutMapping(value = "/{id}/medical-history/{entryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalHistoryResponseDto> updateMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId,
            @Parameter(description = "Patient ID", required = true)
            @RequestParam("patientId") Long patientId,
            @Parameter(description = "Entry date (format: yyyy-MM-dd)", required = true)
            @RequestParam("entryDate") String entryDate,
            @Parameter(description = "Description", required = true)
            @RequestParam("description") String description,
            @Parameter(description = "Prescription ID", required = false)
            @RequestParam(value = "prescriptionId", required = false) Long prescriptionId,
            @Parameter(description = "Treatment ID", required = false)
            @RequestParam(value = "treatmentId", required = false) Long treatmentId,
            @Parameter(description = "File attachment (JPG, PNG, PDF)", required = false)
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            MedicalHistoryRequestDto requestDto = new MedicalHistoryRequestDto();
            requestDto.setPatientId(patientId);
            requestDto.setEntryDate(java.time.LocalDate.parse(entryDate));
            requestDto.setDescription(description);
            requestDto.setPrescriptionId(prescriptionId);
            requestDto.setTreatmentId(treatmentId);
            
            MedicalHistoryResponseDto response = medicalHistoryService.updateMedicalHistoryEntry(entryId, id, requestDto, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating medical history entry", e);
        }
    }

    /**
     * Eliminar Entrada de Historia Clínica
     * Realiza una eliminación lógica de una entrada (campo active = false).
     * La entrada no se borra físicamente, solo se marca como inactiva.
     */
    @DeleteMapping("/{id}/medical-history/{entryId}")
    public ResponseEntity<Void> deleteMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId) {
        medicalHistoryService.deleteMedicalHistoryEntry(entryId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Crear Nuevo Tratamiento
     * El dentista crea un nuevo tratamiento para un paciente (Ej: Ortodoncia, Implante dental).
     * Define el nombre, descripción, fechas estimadas y número de sesiones planificadas.
     * El estado inicial es "pending" hasta que se registre la primera sesión.
     */
    @PostMapping("/{id}/patients/{patientId}/treatments")
    public ResponseEntity<TreatmentResponseDto> createTreatment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TreatmentRequestDto requestDto) {
        try {
            TreatmentResponseDto response = treatmentService.createTreatment(id, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating treatment", e);
        }
    }

    /**
     * Ver Tratamientos de un Paciente
     * Retorna todos los tratamientos activos de un paciente específico.
     * Incluye información de progreso, fechas y estado de cada tratamiento.
     */
    @GetMapping("/{id}/patients/{patientId}/treatments")
    public ResponseEntity<List<TreatmentResponseDto>> getTreatmentsByPatient(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId) {
        List<TreatmentResponseDto> treatments = treatmentService.getTreatmentsByDentistAndPatient(id, patientId);
        return ResponseEntity.ok(treatments);
    }

    /**
     * Ver Detalle Completo de un Tratamiento
     * Retorna la información detallada de un tratamiento incluyendo TODAS las sesiones.
     * Las sesiones son entradas de historia clínica vinculadas a este tratamiento.
     * Muestra progreso, fechas, estado y el listado completo de sesiones realizadas.
     */
    @GetMapping("/{id}/treatments/{treatmentId}")
    public ResponseEntity<TreatmentDetailResponseDto> getTreatmentDetail(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId) {
        TreatmentDetailResponseDto treatment = treatmentService.getTreatmentDetailById(treatmentId, id);
        return ResponseEntity.ok(treatment);
    }

    /**
     * Modificar Tratamiento
     * Actualiza la información general de un tratamiento existente.
     * Permite cambiar nombre, descripción, fechas estimadas, número de sesiones y notas.
     * NO cambia el estado ni las sesiones completadas.
     */
    @PutMapping("/{id}/treatments/{treatmentId}")
    public ResponseEntity<TreatmentResponseDto> updateTreatment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId,
            @Valid @RequestBody TreatmentRequestDto requestDto) {
        try {
            TreatmentResponseDto response = treatmentService.updateTreatment(treatmentId, id, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating treatment", e);
        }
    }

    /**
     * Cambiar Estado del Tratamiento
     * Actualiza únicamente el estado de un tratamiento.
     * Estados válidos: "pendiente", "en progreso", "completado", "cancelado".
     * Si se marca como "completado", automáticamente se establece la fecha real de fin.
     */
    @PutMapping("/{id}/treatments/{treatmentId}/status")
    public ResponseEntity<TreatmentResponseDto> updateTreatmentStatus(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId,
            @Parameter(description = "New status", required = true)
            @RequestParam("status") String status) {
        try {
            TreatmentResponseDto response = treatmentService.updateTreatmentStatus(treatmentId, id, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating treatment status", e);
        }
    }

    /**
     * Eliminar Tratamiento
     * Realiza una eliminación lógica de un tratamiento (campo active = false).
     * El tratamiento no se borra físicamente, solo se marca como inactivo.
     * Las sesiones (entradas de historia clínica) NO se eliminan.
     */
    @DeleteMapping("/{id}/treatments/{treatmentId}")
    public ResponseEntity<Void> deleteTreatment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId) {
        treatmentService.deleteTreatment(treatmentId, id);
        return ResponseEntity.noContent().build();
    }

}
