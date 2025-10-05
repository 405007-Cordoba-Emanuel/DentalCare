export interface DentistResponse {
  id: number;
  firstName: string;
  lastName: string;
  licenseNumber: string;
  specialty: string;
  phone: string;
  email: string;
  address: string;
  active: boolean;
}

export interface DentistUpdateRequest {
  firstName: string;
  lastName: string;
  licenseNumber: string;
  specialty: string;
  phone?: string;
  email?: string;
  address?: string;
  active?: boolean;
}

export interface DentistInfo {
  id: number;
  name: string;
  licenseNumber: string;
  specialty: string;
}
