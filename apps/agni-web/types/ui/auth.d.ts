import type { AuthTokenResponse, LoginRequest } from "../api/auth";

export interface FormLogin extends LoginRequest {}

export interface AuthToken extends AuthTokenResponse {}