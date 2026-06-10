import http from './http'
import type { ApiResponse } from './http'

export interface UploadImageResult {
  url: string
  fileName: string
}

export interface UploadVideoResult {
  url: string
  fileName: string
}

export function uploadImage(file: File): Promise<ApiResponse<UploadImageResult>> {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000,
  }).then((r) => r.data)
}

export function uploadVideo(file: File): Promise<ApiResponse<UploadVideoResult>> {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/upload/video', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000,
  }).then((r) => r.data)
}
