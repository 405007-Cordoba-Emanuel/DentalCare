export interface CreateDentistRequest {
  email: string;
  firstName: string;
  lastName: string;
  licenseNumber: string;
  specialty: string;
}

export interface CreateDentistResponse {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  dentistId: number;
  licenseNumber: string;
  specialty: string;
  userAlreadyExisted: boolean;
  message: string;
}

