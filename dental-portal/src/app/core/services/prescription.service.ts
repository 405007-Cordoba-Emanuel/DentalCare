import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiConfig } from '../config/api.config';

export interface Prescription {
  id: number;
  patientId: number;
  patientName: string;
  patientDni: string;
  dentistId: number;
  dentistName: string;
  dentistLicenseNumber: string;
  dentistSpecialty: string;
  prescriptionDate: string;
  observations: string;
  medications: string;
  active: boolean;
  lastUpdatedDatetime: string;
}

@Injectable({
  providedIn: 'root',
})
export class PrescriptionService {
  private http = inject(HttpClient);
  private apiConfig = inject(ApiConfig);
  private apiUrl = this.apiConfig.corePatientUrl;

  /**
   * Obtiene todas las recetas de un paciente
   */
  getPrescriptionsByPatientId(patientId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.apiUrl}/${patientId}/prescriptions`);
  }

  /**
   * Obtiene una receta específica por ID
   */
  getPrescriptionById(patientId: number, prescriptionId: number): Observable<Prescription> {
    return this.http.get<Prescription>(`${this.apiUrl}/${patientId}/prescriptions/${prescriptionId}`);
  }

  /**
   * Obtiene el conteo de recetas del paciente
   */
  getPrescriptionCount(patientId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/${patientId}/prescriptions/count`);
  }

  // ========== MÉTODOS PARA DENTISTA ==========

  private dentistApiUrl = this.apiConfig.coreDentistUrl;

  /**
   * Obtiene todas las recetas de un paciente específico para un dentista
   */
  getPrescriptionsByDentistIdAndPatientId(dentistId: number, patientId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.dentistApiUrl}/${dentistId}/prescriptions/patient/${patientId}`);
  }

  /**
   * Obtiene una receta específica por ID para un dentista
   */
  getPrescriptionByIdAndDentistId(dentistId: number, prescriptionId: number): Observable<Prescription> {
    return this.http.get<Prescription>(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}`);
  }

  /**
   * Crea una nueva receta
   */
  createPrescription(dentistId: number, prescription: any): Observable<Prescription> {
    return this.http.post<Prescription>(`${this.dentistApiUrl}/${dentistId}/prescriptions`, prescription);
  }

  /**
   * Actualiza una receta existente
   */
  updatePrescription(dentistId: number, prescriptionId: number, prescription: any): Observable<Prescription> {
    return this.http.put<Prescription>(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}`, prescription);
  }

  /**
   * Elimina una receta (soft delete)
   */
  deletePrescription(dentistId: number, prescriptionId: number): Observable<void> {
    return this.http.delete<void>(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}`);
  }


  /**
   * Descarga una receta en formato PDF
   */
  downloadPrescriptionPdf(dentistId: number, prescriptionId: number): Observable<{ blob: Blob; filename: string }> {
    return this.http.get(`${this.dentistApiUrl}/${dentistId}/prescriptions/${prescriptionId}/download-pdf`, {
      responseType: 'blob',
      observe: 'response'
    }).pipe(
      map((response: HttpResponse<Blob>) => {
        const blob = response.body as Blob;
        // Extraer el nombre del archivo del header Content-Disposition
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = '';
        
        if (contentDisposition) {
          // Intentar extraer filename* (RFC 5987) primero - formato: filename*=UTF-8''nombre
          const filenameStarMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/);
          if (filenameStarMatch && filenameStarMatch[1]) {
            try {
              filename = decodeURIComponent(filenameStarMatch[1].trim());
            } catch (e) {
              // Si falla la decodificación, continuar con otros métodos
            }
          }
          
          // Si no se encontró filename*, intentar filename normal
          if (!filename) {
            // Buscar filename="..." o filename='...'
            const quotedMatch = contentDisposition.match(/filename\s*=\s*["']([^"']+)["']/i);
            if (quotedMatch && quotedMatch[1]) {
              filename = quotedMatch[1].trim();
            } else {
              // Buscar filename=nombre (sin comillas)
              const unquotedMatch = contentDisposition.match(/filename\s*=\s*([^;]+)/i);
              if (unquotedMatch && unquotedMatch[1]) {
                filename = unquotedMatch[1].trim();
                // Limpiar espacios y caracteres extra
                filename = filename.replace(/^["']|["']$/g, '');
              }
            }
          }
        }
        
        // Si no se pudo extraer el nombre, lanzar un error con información de debug
        if (!filename) {
          console.error('No se pudo extraer el nombre del archivo del header Content-Disposition');
          console.error('Header recibido:', contentDisposition);
          console.error('Todos los headers:', response.headers.keys());
          throw new Error('No se pudo obtener el nombre del archivo desde el servidor');
        }
        
        return { blob, filename };
      })
    );
  }
}

