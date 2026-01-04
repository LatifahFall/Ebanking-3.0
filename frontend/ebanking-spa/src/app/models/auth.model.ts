/**
 * Auth models mirroring backend DTOs
 */
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  refresh_expires_in: number;
  token_type: string;
  scope: string;
}

export interface RegisterResponse {
  success: boolean;
  message: string;
  userId: string;
}

export interface RefreshRequest {
  refresh_token: string;
}

export interface TokenRequest {
  token: string;
}

export interface TokenInfo {
  sub: string;
  email: string;
  preferred_username: string;
  given_name: string;
  family_name: string;
  roles: string[];
  exp: number;
  iat: number;
}
