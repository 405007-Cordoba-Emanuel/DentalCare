export interface AppointmentRequest {
  patientId: number;
  startDateTime: Date;
  endDateTime: Date;
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
  startDateTime: Date;
  endDateTime: Date;
  durationMinutes: number;
  status: string;
  reason: string;
  notes: string;
  active: boolean;
  createdDatetime: Date;
  lastUpdatedDatetime: Date;
}
