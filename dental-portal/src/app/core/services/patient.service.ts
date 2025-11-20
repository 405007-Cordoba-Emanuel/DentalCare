import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Patient {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  dni: string;
  active: boolean;
}

export interface PatientUpdateRequest {
  dni: string;
  active?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class PatientService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8082/api/core/patient';

  getPatientById(patientId: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.apiUrl}/getById/${patientId}`);
  }

  updatePatient(patientId: number, updateData: PatientUpdateRequest): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/update/${patientId}`, updateData);
  }

  getPatientIdByUserId(userId: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/user-id/${userId}`);
  }

  assignDentistToPatient(patientId: number, dentistId: number): Observable<Patient> {
    return this.http.put<Patient>(`${this.apiUrl}/${patientId}/assign-dentist/${dentistId}`, {});
  }
}

