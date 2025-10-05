export interface TreatmentResponse {
  id: number;
  name: string;
  description: string;
  startDate: string;
  estimatedEndDate: string;
  actualEndDate: string;
  status: string;
  totalSessions: number;
  completedSessions: number;
  notes: string;
  patient: {
    id: number;
    firstName: string;
    lastName: string;
    dni: string;
  };
}

export interface TreatmentDetailResponse {
  id: number;
  name: string;
  description: string;
  startDate: string;
  estimatedEndDate: string;
  actualEndDate: string;
  status: string;
  totalSessions: number;
  completedSessions: number;
  notes: string;
  progressPercentage: number;
  patient: {
    id: number;
    firstName: string;
    lastName: string;
    dni: string;
  };
  sessions: any[]; // Entradas de historia clínica
}
