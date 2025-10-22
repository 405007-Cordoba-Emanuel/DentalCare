export interface PatientSummary {
  id: number;
  firstName: string;
  lastName: string;
  dni: string;
  email: string;
  phone: string;
  active: boolean;
}

export interface DentistPatientsResponse {
  dentistId: number;
  dentistName: string;
  licenseNumber: string;
  specialty: string;
  patients: PatientSummary[];
}

export interface PatientInfo {
  id: number;
  firstName: string;
  lastName: string;
  dni: string;
}

export interface PatientRequest {
  userId: number;
  dni: number;
}
