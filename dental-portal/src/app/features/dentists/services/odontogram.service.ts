import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiConfig } from '../../../core/config/api.config';

export interface ToothData {
  number: number;
  statuses: string[];
}

export interface OdontogramRequestDto {
  patientId: number;
  dentitionType: 'adult' | 'child';
  teethData: string; // JSON string
}

export interface OdontogramResponseDto {
  id: number;
  patientId: number;
  dentitionType: 'adult' | 'child';
  teethData: string; // JSON string
  createdDatetime: string;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class OdontogramService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.coreDentistUrl;

  /**
   * Crear un nuevo odontograma
   */
  createOdontogram(dentistId: number, patientId: number, requestDto: OdontogramRequestDto): Observable<OdontogramResponseDto> {
    return this.http.post<OdontogramResponseDto>(
      `${this.apiUrl}/${dentistId}/patients/${patientId}/odontogram`,
      requestDto
    );
  }

  /**
   * Obtener todos los odontogramas de un paciente
   */
  getOdontogramsByPatient(dentistId: number, patientId: number): Observable<OdontogramResponseDto[]> {
    return this.http.get<OdontogramResponseDto[]>(
      `${this.apiUrl}/${dentistId}/patients/${patientId}/odontogram`
    );
  }

  /**
   * Obtener un odontograma específico por ID
   */
  getOdontogramById(dentistId: number, odontogramId: number): Observable<OdontogramResponseDto> {
    return this.http.get<OdontogramResponseDto>(
      `${this.apiUrl}/${dentistId}/odontogram/${odontogramId}`
    );
  }

  /**
   * Actualizar un odontograma existente
   */
  updateOdontogram(dentistId: number, odontogramId: number, requestDto: OdontogramRequestDto): Observable<OdontogramResponseDto> {
    return this.http.put<OdontogramResponseDto>(
      `${this.apiUrl}/${dentistId}/odontogram/${odontogramId}`,
      requestDto
    );
  }

  /**
   * Eliminar (lógicamente) un odontograma
   */
  deleteOdontogram(dentistId: number, odontogramId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${dentistId}/odontogram/${odontogramId}`
    );
  }

  /**
   * Contar odontogramas de un paciente
   */
  countOdontogramsByPatient(patientId: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/patients/${patientId}/odontogram/count`
    );
  }
}
