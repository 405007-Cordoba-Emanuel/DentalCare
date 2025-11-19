package com.dentalCare.be_core.services.impl;

import com.dentalCare.be_core.config.mapper.ModelMapperUtils;
import com.dentalCare.be_core.dtos.request.odontogram.OdontogramRequestDto;
import com.dentalCare.be_core.dtos.response.odontogram.OdontogramResponseDto;
import com.dentalCare.be_core.entities.Dentist;
import com.dentalCare.be_core.entities.Odontogram;
import com.dentalCare.be_core.entities.Patient;
import com.dentalCare.be_core.repositories.DentistRepository;
import com.dentalCare.be_core.repositories.OdontogramRepository;
import com.dentalCare.be_core.repositories.PatientRepository;
import com.dentalCare.be_core.services.OdontogramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OdontogramServiceImpl implements OdontogramService {

    @Autowired
    private OdontogramRepository odontogramRepository;

    @Autowired
    private DentistRepository dentistRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ModelMapperUtils modelMapperUtils;

    @Override
    public OdontogramResponseDto createOdontogram(Long dentistId, OdontogramRequestDto requestDto) {
        // Validar que el dentista existe
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el dentista con ID: " + dentistId));

        // Validar que el paciente existe
        Patient patient = patientRepository.findById(requestDto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente con ID: " + requestDto.getPatientId()));

        // Validar que el paciente pertenece al dentista
        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("El paciente no pertenece a este dentista");
        }

        // Validar que el paciente esté activo
        if (patient.getActive() == null || !patient.getActive()) {
            throw new IllegalArgumentException("El paciente no está activo");
        }

        // Crear el odontograma
        Odontogram odontogram = new Odontogram();
        odontogram.setPatient(patient);
        odontogram.setDentitionType(requestDto.getDentitionType());
        odontogram.setTeethData(requestDto.getTeethData());
        odontogram.setActive(true);

        Odontogram savedOdontogram = odontogramRepository.save(odontogram);
        log.info("Odontograma creado con ID: {} para paciente ID: {}", savedOdontogram.getId(), patient.getId());
        
        return mapToResponseDto(savedOdontogram);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OdontogramResponseDto> getOdontogramsByPatient(Long dentistId, Long patientId) {
        // Validar que el dentista existe
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el dentista con ID: " + dentistId));

        // Validar que el paciente existe
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el paciente con ID: " + patientId));

        // Validar que el paciente pertenece al dentista
        if (!patient.getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("El paciente no pertenece a este dentista");
        }

        List<Odontogram> odontograms = odontogramRepository.findByPatientIdAndActiveTrue(patientId);
        return odontograms.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OdontogramResponseDto getOdontogramById(Long odontogramId, Long dentistId) {
        // Validar que el dentista existe
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el dentista con ID: " + dentistId));

        // Buscar el odontograma
        Odontogram odontogram = odontogramRepository.findById(odontogramId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el odontograma con ID: " + odontogramId));

        // Validar que está activo
        if (odontogram.getActive() == null || !odontogram.getActive()) {
            throw new IllegalArgumentException("El odontograma no está activo");
        }

        // Validar que el paciente del odontograma pertenece al dentista
        if (!odontogram.getPatient().getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("El odontograma no pertenece a un paciente de este dentista");
        }

        return mapToResponseDto(odontogram);
    }

    @Override
    public OdontogramResponseDto updateOdontogram(Long odontogramId, Long dentistId, OdontogramRequestDto requestDto) {
        // Validar que el dentista existe
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el dentista con ID: " + dentistId));

        // Buscar el odontograma
        Odontogram odontogram = odontogramRepository.findById(odontogramId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el odontograma con ID: " + odontogramId));

        // Validar que está activo
        if (odontogram.getActive() == null || !odontogram.getActive()) {
            throw new IllegalArgumentException("El odontograma no está activo");
        }

        // Validar que el paciente del odontograma pertenece al dentista
        if (!odontogram.getPatient().getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("El odontograma no pertenece a un paciente de este dentista");
        }

        // Actualizar los datos
        odontogram.setDentitionType(requestDto.getDentitionType());
        odontogram.setTeethData(requestDto.getTeethData());

        Odontogram updatedOdontogram = odontogramRepository.save(odontogram);
        log.info("Odontograma actualizado con ID: {}", updatedOdontogram.getId());
        
        return mapToResponseDto(updatedOdontogram);
    }

    @Override
    public void deleteOdontogram(Long odontogramId, Long dentistId) {
        // Validar que el dentista existe
        Dentist dentist = dentistRepository.findById(dentistId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el dentista con ID: " + dentistId));

        // Buscar el odontograma
        Odontogram odontogram = odontogramRepository.findById(odontogramId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el odontograma con ID: " + odontogramId));

        // Validar que está activo
        if (odontogram.getActive() == null || !odontogram.getActive()) {
            throw new IllegalArgumentException("El odontograma ya está inactivo");
        }

        // Validar que el paciente del odontograma pertenece al dentista
        if (!odontogram.getPatient().getDentist().getId().equals(dentistId)) {
            throw new IllegalArgumentException("El odontograma no pertenece a un paciente de este dentista");
        }

        // Eliminación lógica
        odontogram.setActive(false);
        odontogramRepository.save(odontogram);
        log.info("Odontograma eliminado (lógicamente) con ID: {}", odontogramId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOdontogramsByPatient(Long patientId) {
        return odontogramRepository.countByPatientIdAndActiveTrue(patientId);
    }

    /**
     * Mapea un Odontogram entity a OdontogramResponseDto
     */
    private OdontogramResponseDto mapToResponseDto(Odontogram odontogram) {
        OdontogramResponseDto dto = new OdontogramResponseDto();
        dto.setId(odontogram.getId());
        dto.setPatientId(odontogram.getPatient().getId());
        dto.setDentitionType(odontogram.getDentitionType());
        dto.setTeethData(odontogram.getTeethData());
        dto.setCreatedAt(odontogram.getCreatedAt());
        dto.setActive(odontogram.getActive());
        return dto;
    }
}

