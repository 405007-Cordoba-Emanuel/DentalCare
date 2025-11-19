export interface UserDetailResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  picture: string | null;
  profileImage: string | null;
  role: 'ADMIN' | 'DENTIST' | 'PATIENT';
  phone: string | null;
  address: string | null;
  birthDate: string | null;
  isActive: boolean;
  lastLogin: string | null;
}

