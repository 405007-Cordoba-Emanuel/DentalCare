package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.patient.PatientUpdateRequestDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.services.PatientService;
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


    @GetMapping("/getById/{id}")
    public ResponseEntity<PatientResponseDto> getById(
            @Parameter(description = "Patient ID", required = true)
            @PathVariable Long id) {
        PatientResponseDto patient = patientService.searchById(id);
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<PatientResponseDto> getByLicenseNumber(
            @Parameter(description = "Patient's DNI", required = true)
            @PathVariable String dni) {

        PatientResponseDto patientResponseDto = patientService.searchByDni(dni);
        return ResponseEntity.ok(patientResponseDto);
    }

    @GetMapping("/getAllActive")
    public ResponseEntity<List<PatientResponseDto>> getAllActive() {
        List<PatientResponseDto> patientResponseDtoList = patientService.findAllActive();
        return ResponseEntity.ok(patientResponseDtoList);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @Parameter(description = "Patient ID to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New data from the patient", required = true)
            @Valid @RequestBody PatientUpdateRequestDto patientUpdateRequestDto) {

        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientUpdateRequestDto);
        return ResponseEntity.ok(patientResponseDto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Patient ID to delete", required = true)
            @PathVariable Long id) {

        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/countActive")
    public ResponseEntity<Long> countActivePatient() {
        long count = patientService.countActivePatient();
        return ResponseEntity.ok(count);
    }
}
