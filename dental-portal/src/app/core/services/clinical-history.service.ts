import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

export interface ClinicalHistoryCount {
  total: number;
  thisMonth: number;
}

@Injectable({
  providedIn: 'root'
})
export class ClinicalHistoryService {
  private apiUrl = 'http://localhost:8082/api/core/patient';

  constructor(private http: HttpClient) {}

  getClinicalHistoryByPatientId(patientId: number): Observable<ClinicalHistoryEntry[]> {
    return this.http.get<ClinicalHistoryEntry[]>(`${this.apiUrl}/${patientId}/clinical-history`);
  }

  getClinicalHistoryEntry(patientId: number, entryId: number): Observable<ClinicalHistoryEntry> {
    return this.http.get<ClinicalHistoryEntry>(`${this.apiUrl}/${patientId}/clinical-history/${entryId}`);
  }

  searchClinicalHistoryByText(patientId: number, searchText: string): Observable<ClinicalHistoryEntry[]> {
    return this.http.get<ClinicalHistoryEntry[]>(`${this.apiUrl}/${patientId}/clinical-history/search`, {
      params: { searchText }
    });
  }

  searchClinicalHistoryByDate(patientId: number, entryDate: string): Observable<ClinicalHistoryEntry[]> {
    return this.http.get<ClinicalHistoryEntry[]>(`${this.apiUrl}/${patientId}/clinical-history/search/date`, {
      params: { entryDate }
    });
  }

  searchClinicalHistoryByDateRange(patientId: number, startDate: string, endDate: string): Observable<ClinicalHistoryEntry[]> {
    return this.http.get<ClinicalHistoryEntry[]>(`${this.apiUrl}/${patientId}/clinical-history/search/date-range`, {
      params: { startDate, endDate }
    });
  }
}

