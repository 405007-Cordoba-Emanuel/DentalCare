export interface AppointmentRequest {
  patientId: number;
  startDateTime: Date | string;
  endDateTime: Date | string;
  reason: string;
  notes: string;
}

export interface AppointmentResponse {
  id: number;
  patientId: number;
  patientName: string;
  patientDni: string;
  dentistId: number;
  dentistName: string;
  dentistLicenseNumber: string;
  dentistSpecialty: string;
  startDateTime: string;
  endDateTime: string;
  durationMinutes: number;
  status: string;
  reason: string;
  notes: string;
  active: boolean;
  createdDatetime: string;
  lastUpdatedDatetime: string;
}
