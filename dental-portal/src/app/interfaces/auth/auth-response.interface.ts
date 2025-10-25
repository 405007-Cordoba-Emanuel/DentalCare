
export interface AuthResponse {
  id: string;
  token: string;
  firstName: string;
  lastName: string;
  email: string;
  picture: string | null;
  role: string;
  dentistId?: number;
  patientId?: number;
  authorized?: boolean;
  authorizationUrl?: string;
}

export interface GoogleAuthRequest {
  idToken: string;
}

export interface AuthorizationCheckResponse {
  authorized: boolean;
  authorizationUrl?: string;
  tokensRefreshed?: boolean;
}

export interface EmailAuthRequest {
  email: string;
  password: string;
}

export interface EmailRegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: string;
}
