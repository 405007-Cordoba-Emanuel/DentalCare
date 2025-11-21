export interface TreatmentResponse {
  id: number;
  patientId: number;
  patientName: string;
  dentistId: number;
  dentistName: string;
  name: string;
  description: string;
  startDate: string;
  estimatedEndDate: string;
  actualEndDate: string | null;
  status: string;
  totalSessions: number;
  completedSessions: number;
  progressPercentage: number;
  notes: string | null;
  active: boolean;
}

export interface TreatmentDetailResponse {
  id: number;
  patientId: number;
  patientName: string;
  dentistId: number;
  dentistName: string;
  name: string;
  description: string;
  startDate: string;
  estimatedEndDate: string;
  actualEndDate: string | null;
  status: string;
  totalSessions: number;
  completedSessions: number;
  notes: string | null;
  progressPercentage: number;
  active: boolean;
  sessions: any[]; // Entradas de historia clínica
}
