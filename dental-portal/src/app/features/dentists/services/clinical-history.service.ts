import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ClinicalHistoryEntry {
  id: number;
  patientId: number;
  patientName: string;
  patientDni: string;
  dentistId: number;
  dentistName: string;
  dentistLicenseNumber: string;
  entryDate: string;
  description: string;
  prescriptionId?: number;
  prescriptionSummary?: string;
  treatmentId?: number;
  treatmentName?: string;
  hasFile: boolean;
  fileUrl?: string;
  fileName?: string;
  fileType?: string;
  active: boolean;
}

export interface ClinicalHistoryRequest {
  patientId: number;
  description: string;
  entryDate?: string; // Opcional, se asigna automáticamente si no se proporciona
}

@Injectable({
  providedIn: 'root'
})
export class DentistClinicalHistoryService {
  private apiUrl = 'http://localhost:8082/api/core/dentist';

  constructor(private http: HttpClient) {}

  getClinicalHistoryByPatient(dentistId: number, patientId: number): Observable<ClinicalHistoryEntry[]> {
    return this.http.get<ClinicalHistoryEntry[]>(`${this.apiUrl}/${dentistId}/patients/${patientId}/clinical-history`);
  }

  getClinicalHistoryEntry(dentistId: number, patientId: number, entryId: number): Observable<ClinicalHistoryEntry> {
    return this.http.get<ClinicalHistoryEntry>(`${this.apiUrl}/${dentistId}/patients/${patientId}/clinical-history/${entryId}`);
  }

  createClinicalHistoryEntry(
    dentistId: number,
    patientId: number,
    description: string,
    file?: File
  ): Observable<ClinicalHistoryEntry> {
    const formData = new FormData();
    formData.append('description', description);
    if (file) {
      formData.append('file', file);
    }
    
    return this.http.post<ClinicalHistoryEntry>(
      `${this.apiUrl}/${dentistId}/patients/${patientId}/clinical-history`,
      formData
    );
  }

  updateClinicalHistoryEntry(
    dentistId: number,
    entryId: number,
    patientId: number,
    description: string,
    file?: File
  ): Observable<ClinicalHistoryEntry> {
    const formData = new FormData();
    formData.append('patientId', patientId.toString());
    formData.append('description', description);
    if (file) {
      formData.append('file', file);
    }
    
    return this.http.put<ClinicalHistoryEntry>(
      `${this.apiUrl}/${dentistId}/clinical-history/${entryId}`,
      formData
    );
  }

  deleteClinicalHistoryEntry(dentistId: number, entryId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${dentistId}/clinical-history/${entryId}`);
  }

  searchClinicalHistoryByText(dentistId: number, patientId: number, searchText: string): Observable<ClinicalHistoryEntry[]> {
    const params = new HttpParams().set('searchText', searchText);
    return this.http.get<ClinicalHistoryEntry[]>(
      `${this.apiUrl}/${dentistId}/patients/${patientId}/clinical-history/search`,
      { params }
    );
  }

  searchClinicalHistoryByDate(dentistId: number, patientId: number, entryDate: string): Observable<ClinicalHistoryEntry[]> {
    const params = new HttpParams().set('entryDate', entryDate);
    return this.http.get<ClinicalHistoryEntry[]>(
      `${this.apiUrl}/${dentistId}/patients/${patientId}/clinical-history/search/date`,
      { params }
    );
  }

  searchClinicalHistoryByDateRange(
    dentistId: number,
    patientId: number,
    startDate: string,
    endDate: string
  ): Observable<ClinicalHistoryEntry[]> {
    const params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    return this.http.get<ClinicalHistoryEntry[]>(
      `${this.apiUrl}/${dentistId}/patients/${patientId}/clinical-history/search/date-range`,
      { params }
    );
  }
}

