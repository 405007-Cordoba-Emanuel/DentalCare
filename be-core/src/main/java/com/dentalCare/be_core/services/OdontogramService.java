package com.dentalCare.be_core.services;

import com.dentalCare.be_core.dtos.request.odontogram.OdontogramRequestDto;
import com.dentalCare.be_core.dtos.response.odontogram.OdontogramResponseDto;

import java.util.List;

public interface OdontogramService {
    
    /**
     * Crear un nuevo odontograma para un paciente
     * @param dentistId ID del dentista que crea el odontograma
     * @param requestDto Datos del odontograma
     * @return Odontograma creado
     */
    OdontogramResponseDto createOdontogram(Long dentistId, OdontogramRequestDto requestDto);
    
    /**
     * Obtener todos los odontogramas de un paciente
     * @param dentistId ID del dentista
     * @param patientId ID del paciente
     * @return Lista de odontogramas del paciente
     */
    List<OdontogramResponseDto> getOdontogramsByPatient(Long dentistId, Long patientId);
    
    /**
     * Obtener un odontograma específico
     * @param odontogramId ID del odontograma
     * @param dentistId ID del dentista
     * @return Odontograma encontrado
     */
    OdontogramResponseDto getOdontogramById(Long odontogramId, Long dentistId);
    
    /**
     * Actualizar un odontograma existente
     * @param odontogramId ID del odontograma
     * @param dentistId ID del dentista
     * @param requestDto Nuevos datos del odontograma
     * @return Odontograma actualizado
     */
    OdontogramResponseDto updateOdontogram(Long odontogramId, Long dentistId, OdontogramRequestDto requestDto);
    
    /**
     * Eliminar (lógicamente) un odontograma
     * @param odontogramId ID del odontograma
     * @param dentistId ID del dentista
     */
    void deleteOdontogram(Long odontogramId, Long dentistId);
    
    /**
     * Contar odontogramas de un paciente
     * @param patientId ID del paciente
     * @return Cantidad de odontogramas activos
     */
    Long countOdontogramsByPatient(Long patientId);
}

