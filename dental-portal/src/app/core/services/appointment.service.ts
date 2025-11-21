import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AppointmentResponse } from '../../features/dentists/interfaces/appointment.interface';
import { DentistService } from './dentist.service';
import { PatientService } from './patient.service';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private dentistService = inject(DentistService);
  private patientService = inject(PatientService);

  /**
   * Obtiene todas las appointments activas (excluyendo canceladas) para un dentista
   */
  getActiveAppointmentsByDentistId(dentistId: number): Observable<AppointmentResponse[]> {
    return this.dentistService.getActiveAppointmentsByDentistId(dentistId);
  }

  /**
   * Obtiene todas las appointments activas (excluyendo canceladas) para un paciente
   */
  getActiveAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.patientService.getActiveAppointmentsByPatientId(patientId);
  }

  /**
   * Obtiene todas las appointments de un dentista (incluyendo canceladas)
   */
  getAllAppointmentsByDentistId(dentistId: number): Observable<AppointmentResponse[]> {
    return this.dentistService.getAppointmentsByDentistId(dentistId);
  }

  /**
   * Obtiene todas las appointments de un paciente (incluyendo canceladas)
   */
  getAllAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.patientService.getAppointmentsByPatientId(patientId);
  }

  /**
   * Obtiene próximas appointments de un paciente
   */
  getUpcomingAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.patientService.getUpcomingAppointmentsByPatientId(patientId);
  }

  /**
   * Obtiene appointments pasadas de un paciente
   */
  getPastAppointmentsByPatientId(patientId: number): Observable<AppointmentResponse[]> {
    return this.patientService.getPastAppointmentsByPatientId(patientId);
  }

  /**
   * Obtiene appointments según el rol y el ID
   * @param role - 'DENTIST' o 'PATIENT'
   * @param id - ID del dentista o paciente
   * @param includeCancelled - Si se deben incluir las appointments canceladas
   */
  getAppointmentsByRoleAndId(role: string, id: number, includeCancelled: boolean = false): Observable<AppointmentResponse[]> {
    if (role === 'DENTIST') {
      return includeCancelled 
        ? this.getAllAppointmentsByDentistId(id)
        : this.getActiveAppointmentsByDentistId(id);
    } else if (role === 'PATIENT') {
      return includeCancelled 
        ? this.getAllAppointmentsByPatientId(id)
        : this.getActiveAppointmentsByPatientId(id);
    } else {
      throw new Error('Invalid role. Must be DENTIST or PATIENT');
    }
  }
}

// Re-export tipos para conveniencia
export type Appointment = AppointmentResponse;
export enum AppointmentStatus {
  PROGRAMADO = 'PROGRAMADO',
  CONFIRMADO = 'CONFIRMADO',
  COMPLETADO = 'COMPLETADO',
  CANCELADO = 'CANCELADO',
  AUSENTE = 'AUSENTE'
}
