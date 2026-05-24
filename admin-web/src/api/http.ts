import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { generateTraceId } from '@/utils/traceId'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  error?: { code: string; message: string }
  traceId?: string
}

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

const http: AxiosInstance = axios.create({
  baseURL: '/api/v1/admin',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  config.headers['X-Trace-Id'] = generateTraceId()
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    const traceId = data?.traceId ? ` (${data.traceId})` : ''

    if (status === 401) {
      const auth = useAuthStore()
      auth.logout()
      router.push('/login')
      ElMessage.error('\u4f1a\u8bdd\u5df2\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55')
    } else if (status === 403) {
      ElMessage.error('\u65e0\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c')
    } else if (status === 404) {
      ElMessage.warning(`\u63a5\u53e3\u4e0d\u5b58\u5728${traceId}`)
    } else if (status >= 500) {
      const msg = data?.error?.message || '\u670d\u52a1\u5f02\u5e38'
      ElMessage.error(`${msg}${traceId}`)
    } else if (!status) {
      ElMessage.error('\u7f51\u7edc\u8fde\u63a5\u5f02\u5e38')
    }
    return Promise.reject(error)
  }
)

export default http
