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
  dentistLicenseNumber?: string;
  dentistSpecialty?: string;
  // Formato general (startDateTime y endDateTime combinados)
  startDateTime?: string;
  endDateTime?: string;
  // Formato de calendario (date, startTime, endTime separados)
  date?: string;
  startTime?: string;
  endTime?: string;
  durationMinutes: number;
  status: string;
  reason: string;
  notes: string;
  active: boolean;
  createdDatetime?: string;
  lastUpdatedDatetime?: string;
}
