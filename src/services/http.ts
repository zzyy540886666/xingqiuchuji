const baseURL = import.meta.env.VITE_API_BASE_URL || 'https://api.example.com/api/v1'

interface RequestOptions<T> {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: T
  auth?: boolean
}

export function request<TResponse, TBody = unknown>(options: RequestOptions<TBody>): Promise<TResponse> {
  const token = uni.getStorageSync('sessionToken')

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${baseURL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(options.auth && token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: response => {
        if (response.statusCode === 401) {
          uni.removeStorageSync('sessionToken')
          reject(new Error('登录已过期，请重新登录'))
          return
        }
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new Error(`请求失败：${response.statusCode}`))
          return
        }
        resolve(response.data as TResponse)
      },
      fail: err => reject(new Error(err.errMsg || '网络异常'))
    })
  })
}
