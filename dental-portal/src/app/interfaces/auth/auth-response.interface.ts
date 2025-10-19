
export interface AuthResponse {
  id: number;
  token: string;
  firstName: string;
  lastName: string;
  email: string;
  picture: string | null;
  role: string;
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
