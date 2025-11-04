package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.CreateDentistFromUserRequest;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.request.medicalhistory.MedicalHistoryRequestDto;
import com.dentalCare.be_core.dtos.request.prescription.PrescriptionRequestDto;
import com.dentalCare.be_core.dtos.request.treatment.TreatmentRequestDto;
import com.dentalCare.be_core.dtos.request.appointment.AppointmentRequestDto;
import com.dentalCare.be_core.dtos.request.appointment.AppointmentUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistResponseDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistPatientsResponseDto;
import com.dentalCare.be_core.dtos.response.medicalhistory.MedicalHistoryResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.dtos.response.prescription.PrescriptionResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentDetailResponseDto;
import com.dentalCare.be_core.dtos.response.treatment.TreatmentResponseDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentResponseDto;
import com.dentalCare.be_core.dtos.response.appointment.AppointmentCalendarDto;
import com.dentalCare.be_core.services.DentistService;
import com.dentalCare.be_core.services.MedicalHistoryService;
import com.dentalCare.be_core.services.PrescriptionPdfService;
import com.dentalCare.be_core.services.PrescriptionService;
import com.dentalCare.be_core.services.UserServiceClient;
import com.dentalCare.be_core.services.TreatmentService;
import com.dentalCare.be_core.services.AppointmentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/core/dentist")
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

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserServiceClient userServiceClient;

    // ------- Bloque Dentista -------
    /**
     * Alta de Dentista
     * Crea un nuevo dentista en el sistema con todos sus datos personales y profesionales.
     * Valida que no exista otro dentista con la misma matrícula o email.
     */
    @Operation(summary = "Crear un nuevo dentista")
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
    @Operation(summary = "Obtener dentista por ID")
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
    @Operation(summary = "Obtener dentista por matrícula")
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
    @Operation(summary = "Listar dentistas activos")
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
    @Operation(summary = "Listar dentistas por especialidad")
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
    @Operation(summary = "Actualizar dentista")
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
    @Operation(summary = "Eliminar dentista")
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
    @Operation(summary = "Contar dentistas activos")
    @GetMapping("/countActive")
    public ResponseEntity<Long> countActiveDentist() {
        long count = dentistService.countActiveDentist();
        return ResponseEntity.ok(count);
    }

    // ------- Bloque Pacientes de Dentista -------
    /**
     * Ver Todos los Pacientes de un Dentista
     * Retorna la lista completa de pacientes asignados a un dentista específico.
     * Incluye tanto pacientes activos como inactivos.
     */
    @Operation(summary = "Listar pacientes de un dentista")
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
    @Operation(summary = "Listar pacientes activos de un dentista")
    @GetMapping("/{id}/patients/active")
    public ResponseEntity<DentistPatientsResponseDto> getActivePatientsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistPatientsResponseDto response = dentistService.getActivePatientsByDentistId(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Crear dentista automáticamente desde registro de usuario
     * Endpoint público para ser llamado desde el microservicio de usuarios
     */
    @Operation(summary = "Crear dentista automáticamente desde registro")
    @PostMapping("/create-from-user")
    public ResponseEntity<DentistResponseDto> createDentistFromUser(
            @Parameter(description = "Datos del usuario") 
            @Valid @RequestBody CreateDentistFromUserRequest request) {
        try {
            log.info("Received request to create dentist from user: {}", request);
            DentistResponseDto createdDentist = dentistService.createDentistFromUser(request);
            log.info("Dentist created successfully: {}", createdDentist);
            return ResponseEntity.ok(createdDentist);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Internal error creating dentist from user", e);
            throw new RuntimeException("Internal error creating dentist from user", e);
        }
    }

    @Operation(summary = "Obtener usuarios pacientes disponibles")
    @GetMapping("/available-patients")
    public ResponseEntity<List<PatientResponseDto>> getAvailablePatientUsers() {
        try {
            List<PatientResponseDto> availableUsers = dentistService.getAvailablePatientUsers();
            return ResponseEntity.ok(availableUsers);
        } catch (Exception e) {
            throw new RuntimeException("Internal error getting available patient users", e);
        }
    }

    @Operation(summary = "Obtener ID de dentista por userId")
    @GetMapping("/user-id/{userId}")
    public ResponseEntity<Long> getDentistIdByUserId(@PathVariable Long userId) {
        try {
            Long dentistId = dentistService.getDentistIdByUserId(userId);
            if (dentistId == null) {
                throw new IllegalArgumentException("No dentist found for userId: " + userId);
            }
            return ResponseEntity.ok(dentistId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error getting dentist ID by userId", e);
        }
    }

    // ------- Bloque Recetas -------
    /**
     * Emitir Receta Médica
     * El dentista crea una receta/prescripción médica para un paciente específico.
     * Incluye medicamentos, observaciones y fecha de emisión.
     */
    @Operation(summary = "Crear receta para un paciente")
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
    @Operation(summary = "Listar recetas de un dentista")
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
    @Operation(summary = "Listar recetas por paciente")
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
    @Operation(summary = "Obtener receta por ID")
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
    @Operation(summary = "Actualizar receta")
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
    @Operation(summary = "Eliminar receta")
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
    @Operation(summary = "Contar recetas de un dentista")
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

            // Construir nombre de archivo: Receta_YYYY-MM-DD_Nombre_Apellido.pdf
            String datePart = prescription.getPrescriptionDate() != null
                    ? prescription.getPrescriptionDate().toString()
                    : java.time.LocalDate.now().toString();
            com.dentalCare.be_core.dtos.external.UserDetailDto patientUser =
                    userServiceClient.getUserById(prescription.getPatient().getUserId());
            String firstName = patientUser.getFirstName() != null ? patientUser.getFirstName() : "Paciente";
            String lastName = patientUser.getLastName() != null ? patientUser.getLastName() : "";
            String rawFileName = String.format("Receta_%s_%s_%s.pdf", datePart, firstName, lastName).trim();
            // Sanitizar para encabezado HTTP (evitar espacios/acentos problemáticos)
            String safeFileName = rawFileName
                    .replaceAll("[\\\\/:*?\"<>|]", "-")
                    .replaceAll("\\s+", "_");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                    .filename(safeFileName)
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

    // ------- Bloque Historia Clínica -------
    /**
     * Crear Entrada en Historia Clínica
     * El dentista registra una nueva entrada en la historia clínica del paciente.
     * Incluye descripción, opcionalmente receta, tratamiento y archivo adjunto (foto/PDF).
     * La fecha se asigna automáticamente al momento de la creación.
     * Los campos se envían como form-data individual compatible con Swagger.
     */
    @Operation(summary = "Crear entrada de historia clínica")
    @PostMapping(value = "/{id}/patients/{patientId}/clinical-history", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalHistoryResponseDto> createMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId,
            @Parameter(description = "Entry date (format: yyyy-MM-dd) - Opcional, se asigna automáticamente si no se proporciona", required = false)
            @RequestParam(value = "entryDate", required = false) String entryDate,
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
            if (entryDate != null && !entryDate.isEmpty()) {
                requestDto.setEntryDate(java.time.LocalDate.parse(entryDate));
            }
            // Si no se proporciona entryDate, se asignará automáticamente en @PrePersist
            requestDto.setDescription(description);
            requestDto.setPrescriptionId(prescriptionId);
            requestDto.setTreatmentId(treatmentId);
            
            MedicalHistoryResponseDto response = medicalHistoryService.createMedicalHistoryEntry(id, requestDto, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating clinical history entry", e);
        }
    }

    /**
     * Ver Historia Clínica Completa de un Paciente
     * Retorna todas las entradas de la historia clínica de un paciente específico.
     * Las entradas se ordenan por fecha descendente (más recientes primero).
     */
    @Operation(summary = "Listar historia clínica de un paciente")
    @GetMapping("/{id}/patients/{patientId}/clinical-history")
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
    @Operation(summary = "Obtener entrada de historia clínica por ID")
    @GetMapping("/{id}/clinical-history/{entryId}")
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
     * Actualiza una entrada existente de la historia clínica (descripción, archivo, etc.).
     * La fecha no se puede modificar (se mantiene la fecha de creación).
     * Si se envía un nuevo archivo, reemplaza el anterior. Los campos se envían individualmente.
     */
    @Operation(summary = "Actualizar entrada de historia clínica")
    @PutMapping(value = "/{id}/clinical-history/{entryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalHistoryResponseDto> updateMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId,
            @Parameter(description = "Patient ID", required = true)
            @RequestParam("patientId") Long patientId,
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
            // Entry date no se actualiza - se mantiene la fecha de creación original
            requestDto.setDescription(description);
            requestDto.setPrescriptionId(prescriptionId);
            requestDto.setTreatmentId(treatmentId);
            
            MedicalHistoryResponseDto response = medicalHistoryService.updateMedicalHistoryEntry(entryId, id, requestDto, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating clinical history entry", e);
        }
    }

    /**
     * Eliminar Entrada de Historia Clínica
     * Realiza una eliminación lógica de una entrada (campo active = false).
     * La entrada no se borra físicamente, solo se marca como inactiva.
     */
    @Operation(summary = "Eliminar entrada de historia clínica")
    @DeleteMapping("/{id}/clinical-history/{entryId}")
    public ResponseEntity<Void> deleteMedicalHistoryEntry(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Entry ID", required = true)
            @PathVariable Long entryId) {
        medicalHistoryService.deleteMedicalHistoryEntry(entryId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Buscar Historia Clínica por Texto
     * Busca entradas de historia clínica por texto en la descripción.
     * Si el texto está vacío, retorna todas las entradas.
     */
    @Operation(summary = "Buscar historia clínica por texto")
    @GetMapping("/{id}/patients/{patientId}/clinical-history/search")
    public ResponseEntity<List<MedicalHistoryResponseDto>> searchClinicalHistoryByText(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId,
            @Parameter(description = "Texto a buscar en la descripción", required = true)
            @RequestParam("searchText") String searchText) {
        List<MedicalHistoryResponseDto> results = medicalHistoryService.searchByText(id, patientId, searchText);
        return ResponseEntity.ok(results);
    }

    /**
     * Buscar Historia Clínica por Fecha
     * Busca entradas de historia clínica por fecha específica.
     */
    @Operation(summary = "Buscar historia clínica por fecha")
    @GetMapping("/{id}/patients/{patientId}/clinical-history/search/date")
    public ResponseEntity<List<MedicalHistoryResponseDto>> searchClinicalHistoryByDate(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId,
            @Parameter(description = "Fecha a buscar (format: yyyy-MM-dd)", required = true)
            @RequestParam("entryDate") String entryDate) {
        LocalDate date = LocalDate.parse(entryDate);
        List<MedicalHistoryResponseDto> results = medicalHistoryService.searchByDate(id, patientId, date);
        return ResponseEntity.ok(results);
    }

    /**
     * Buscar Historia Clínica por Rango de Fechas
     * Busca entradas de historia clínica dentro de un rango de fechas.
     */
    @Operation(summary = "Buscar historia clínica por rango de fechas")
    @GetMapping("/{id}/patients/{patientId}/clinical-history/search/date-range")
    public ResponseEntity<List<MedicalHistoryResponseDto>> searchClinicalHistoryByDateRange(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId,
            @Parameter(description = "Fecha de inicio (format: yyyy-MM-dd)", required = true)
            @RequestParam("startDate") String startDate,
            @Parameter(description = "Fecha de fin (format: yyyy-MM-dd)", required = true)
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<MedicalHistoryResponseDto> results = medicalHistoryService.searchByDateRange(id, patientId, start, end);
        return ResponseEntity.ok(results);
    }

    // ------- Bloque Tratamientos -------
    /**
     * Crear Nuevo Tratamiento
     * El dentista crea un nuevo tratamiento para un paciente (Ej: Ortodoncia, Implante dental).
     * Define el nombre, descripción, fechas estimadas y número de sesiones planificadas.
     * El estado inicial es "pending" hasta que se registre la primera sesión.
     */
    @Operation(summary = "Crear tratamiento para un paciente")
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
    @Operation(summary = "Listar tratamientos de un paciente")
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
    @Operation(summary = "Obtener detalle de tratamiento")
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
    @Operation(summary = "Actualizar tratamiento")
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
    @Operation(summary = "Cambiar estado del tratamiento")
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
    @Operation(summary = "Eliminar tratamiento")
    @DeleteMapping("/{id}/treatments/{treatmentId}")
    public ResponseEntity<Void> deleteTreatment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Treatment ID", required = true)
            @PathVariable Long treatmentId) {
        treatmentService.deleteTreatment(treatmentId, id);
        return ResponseEntity.noContent().build();
    }

    // ------- Bloque Turnos -------
    /**
     * Crear Nuevo Turno
     * El dentista crea un nuevo turno para uno de sus pacientes.
     * Define fecha/hora de inicio, duración, motivo y observaciones.
     * Valida que no haya conflictos de horario con otros turnos.
     */
    @Operation(summary = "Crear turno para un paciente")
    @PostMapping("/{id}/appointments")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDto appointmentRequestDto) {
        try {
            AppointmentResponseDto appointment = appointmentService.createAppointmentForDentist(id, appointmentRequestDto);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error creating appointment", e);
        }
    }

    /**
     * Ver Todos los Turnos del Dentista
     * Retorna todos los turnos del dentista ordenados por fecha/hora de inicio.
     * Incluye turnos de todos los pacientes y todos los estados.
     */
    @Operation(summary = "Listar turnos del dentista")
    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        List<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByDentistId(id);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Ver Turnos de un Paciente Específico
     * Retorna todos los turnos que el dentista tiene con un paciente en particular.
     * Útil para ver el historial de citas de un paciente específico.
     */
    @Operation(summary = "Listar turnos de un paciente")
    @GetMapping("/{id}/appointments/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointmentsByPatientId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long patientId) {
        List<AppointmentResponseDto> appointments = appointmentService.getAppointmentsByDentistIdAndPatientId(id, patientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Ver Detalle de un Turno
     * Obtiene la información completa de un turno específico.
     * Incluye datos del paciente, fecha/hora, motivo y observaciones.
     */
    @Operation(summary = "Obtener turno por ID")
    @GetMapping("/{id}/appointments/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long appointmentId) {
        AppointmentResponseDto appointment = appointmentService.getAppointmentByIdAndDentistId(appointmentId, id);
        return ResponseEntity.ok(appointment);
    }

    /**
     * Modificar Turno Existente
     * Actualiza los datos de un turno (fecha/hora, motivo, observaciones).
     * Valida que no haya conflictos de horario con otros turnos.
     */
    @Operation(summary = "Actualizar turno")
    @PutMapping("/{id}/appointments/{appointmentId}")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentUpdateRequestDto appointmentUpdateRequestDto) {
        try {
            AppointmentResponseDto appointment = appointmentService.updateAppointment(appointmentId, id, appointmentUpdateRequestDto);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating appointment", e);
        }
    }

    /**
     * Cambiar Estado del Turno
     * Actualiza únicamente el estado de un turno.
     * Estados válidos: SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW.
     */
    @Operation(summary = "Cambiar estado del turno")
    @PutMapping("/{id}/appointments/{appointmentId}/status")
    public ResponseEntity<AppointmentResponseDto> updateAppointmentStatus(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long appointmentId,
            @Parameter(description = "New status", required = true)
            @RequestParam("status") com.dentalCare.be_core.entities.AppointmentStatus status) {
        try {
            AppointmentResponseDto appointment = appointmentService.updateAppointmentStatus(appointmentId, id, status);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Internal error updating appointment status", e);
        }
    }

    /**
     * Cancelar Turno
     * Realiza una eliminación lógica de un turno (campo active = false).
     * El turno no se borra físicamente, solo se marca como inactivo.
     */
    @Operation(summary = "Cancelar turno")
    @DeleteMapping("/{id}/appointments/{appointmentId}")
    public ResponseEntity<Void> cancelAppointment(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Appointment ID", required = true)
            @PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(appointmentId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ver Agenda Mensual
     * Retorna todos los turnos del dentista para un mes específico.
     * Formato optimizado para mostrar en calendario mensual.
     */
    @Operation(summary = "Listar turnos del mes")
    @GetMapping("/{id}/appointments/month")
    public ResponseEntity<List<AppointmentCalendarDto>> getMonthlyAppointments(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Year", required = true)
            @RequestParam int year,
            @Parameter(description = "Month (1-12)", required = true)
            @RequestParam int month) {
        List<AppointmentCalendarDto> appointments = appointmentService.getMonthlyAppointments(id, year, month);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Ver Agenda Semanal
     * Retorna todos los turnos del dentista para una semana específica.
     * Formato optimizado para mostrar en vista semanal.
     */
    @Operation(summary = "Listar turnos de la semana")
    @GetMapping("/{id}/appointments/week")
    public ResponseEntity<List<AppointmentCalendarDto>> getWeeklyAppointments(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Start date of the week", required = true)
            @RequestParam LocalDate startDate) {
        List<AppointmentCalendarDto> appointments = appointmentService.getWeeklyAppointments(id, startDate);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Ver Agenda Diaria
     * Retorna todos los turnos del dentista para un día específico.
     * Formato optimizado para mostrar en vista diaria.
     */
    @Operation(summary = "Listar turnos del día")
    @GetMapping("/{id}/appointments/day")
    public ResponseEntity<List<AppointmentCalendarDto>> getDailyAppointments(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Date", required = true)
            @RequestParam LocalDate date) {
        List<AppointmentCalendarDto> appointments = appointmentService.getDailyAppointments(id, date);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Verificar Conflicto de Horario
     * Verifica si existe conflicto de horario para un dentista en un rango de tiempo específico.
     * Útil para validar disponibilidad antes de crear/modificar turnos.
     */
    @Operation(summary = "Verificar conflicto de horario")
    @GetMapping("/{id}/appointments/conflict-check")
    public ResponseEntity<Boolean> checkTimeConflict(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Start time (format: yyyy-MM-ddTHH:mm:ss)", required = true)
            @RequestParam String startTime,
            @Parameter(description = "End time (format: yyyy-MM-ddTHH:mm:ss)", required = true)
            @RequestParam String endTime) {
        // Parse the datetime strings
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startTime);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endTime);
        
        boolean hasConflict = appointmentService.hasTimeConflict(id, start, end);
        return ResponseEntity.ok(hasConflict);
    }

    /**
     * Contar Turnos por Estado
     * Retorna el número de turnos del dentista filtrado por estado específico.
     * Si no se especifica estado, cuenta todos los turnos activos.
     */
    @Operation(summary = "Contar turnos por estado")
    @GetMapping("/{id}/appointments/count")
    public ResponseEntity<Long> countAppointmentsByStatus(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Status filter (optional)", required = false)
            @RequestParam(required = false) com.dentalCare.be_core.entities.AppointmentStatus status) {
        long count;
        if (status != null) {
            count = appointmentService.countAppointmentsByDentistIdAndStatus(id, status);
        } else {
            count = appointmentService.getAppointmentsByDentistId(id).size();
        }
        return ResponseEntity.ok(count);
    }

}
