package com.dentalCare.be_core.controllers;

import com.dentalCare.be_core.dtos.request.dentist.DentistRequestDto;
import com.dentalCare.be_core.dtos.request.dentist.DentistUpdateRequestDto;
import com.dentalCare.be_core.dtos.request.patient.PatientRequestDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistResponseDto;
import com.dentalCare.be_core.dtos.response.dentist.DentistPatientsResponseDto;
import com.dentalCare.be_core.dtos.response.patient.PatientResponseDto;
import com.dentalCare.be_core.services.DentistService;
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
@RequestMapping("/api/dentist")
@Slf4j
@Validated
@Tag(name = "Dentist", description = "API for dentist management")
public class DentistController {

    @Autowired
    private DentistService dentistService;

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

    @GetMapping("/getById/{id}")
    public ResponseEntity<DentistResponseDto> getById(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistResponseDto dentist = dentistService.searchById(id);
        return ResponseEntity.ok(dentist);
    }

    @GetMapping("/licenseNumber/{licenseNumber}")
    public ResponseEntity<DentistResponseDto> getByLicenseNumber(
            @Parameter(description = "Dentist's License Number", required = true)
            @PathVariable String licenseNumber) {

        DentistResponseDto dentistResponseDto = dentistService.searchByLicenseNumber(licenseNumber);
        return ResponseEntity.ok(dentistResponseDto);
    }

    @GetMapping("/getAllActive")
    public ResponseEntity<List<DentistResponseDto>> getAllActive() {
        List<DentistResponseDto> dentistResponseDtoList = dentistService.findAllActive();
        return ResponseEntity.ok(dentistResponseDtoList);
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DentistResponseDto>> getBySpecialty(
            @Parameter(description = "Specialty to look for", required = true)
            @PathVariable String specialty) {

        List<DentistResponseDto> dentistResponseDtoList = dentistService.searchBySpecialty(specialty);
        return ResponseEntity.ok(dentistResponseDtoList);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<DentistResponseDto> updateDentist(
            @Parameter(description = "Dentist ID to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New data from the dentist", required = true)
            @Valid @RequestBody DentistUpdateRequestDto dentistUpdateRequestDto) {

        DentistResponseDto dentistResponseDto = dentistService.updateDentist(id, dentistUpdateRequestDto);
        return ResponseEntity.ok(dentistResponseDto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDentist(
            @Parameter(description = "Dentist ID to delete", required = true)
            @PathVariable Long id) {

        dentistService.deleteDentist(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/countActive")
    public ResponseEntity<Long> countActiveDentist() {
        long count = dentistService.countActiveDentist();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/patients")
    public ResponseEntity<DentistPatientsResponseDto> getPatientsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistPatientsResponseDto response = dentistService.getPatientsByDentistId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/patients/active")
    public ResponseEntity<DentistPatientsResponseDto> getActivePatientsByDentistId(
            @Parameter(description = "Dentist ID", required = true)
            @PathVariable Long id) {
        DentistPatientsResponseDto response = dentistService.getActivePatientsByDentistId(id);
        return ResponseEntity.ok(response);
    }

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
}
