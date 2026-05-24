import http from './http'
import type { ApiResponse } from './http'

export interface LoginParams {
  username: string
  password: string
}

export interface AdminUser {
  id: number
  username: string
  displayName: string
  roles: string[]
  permissions: string[]
}

export interface LoginResult {
  accessToken: string
  expiresIn: number
  displayName: string
  role: string
}

export function login(params: LoginParams): Promise<ApiResponse<LoginResult>> {
  return http.post('/auth/login', params).then((r) => r.data)
}

export function logout(): Promise<ApiResponse<void>> {
  return http.post('/auth/logout').then((r) => r.data)
}

export function getCurrentAdmin(): Promise<ApiResponse<AdminUser>> {
  return http.get('/auth/me').then((r) => r.data)
}
