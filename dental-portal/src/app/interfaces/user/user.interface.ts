export interface UserProfileRequest {
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  birthDate: Date;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  picture: string | null;
  role: string;
  phone?: string | null;
  address?: string | null;
  birthDate?: string | null;
  isActive?: boolean;
  lastLogin?: string | null;
  name?: string;
  token?: string;
  dentistId?: number;
  patientId?: number;
}

export interface UserProfileUpdateRequest {
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  birthDate: Date;
}
