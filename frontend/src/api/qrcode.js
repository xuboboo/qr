import axios from 'axios'

const TOKEN_KEY = 'qr_stat_admin_token'
const USERNAME_KEY = 'qr_stat_admin_username'

const isProd = process.env.NODE_ENV === 'production'

// 生产：页面在 /qr/ 下，API 用 /qr/api 绝对路径（不管有没有尾斜杠都稳定）
// 开发：用代理 /api → localhost:8080
const request = axios.create({
  baseURL: isProd ? '/qr/api' : '/api',
  timeout: 15000
})

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getUsername() {
  return localStorage.getItem(USERNAME_KEY) || ''
}

export function saveAuth(token, username) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USERNAME_KEY, username || '')
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USERNAME_KEY)
}

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data && data.success === false) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      clearAuth()
      window.dispatchEvent(new Event('qr-auth-expired'))
    }

    const message = error.response && error.response.data && error.response.data.message
      ? error.response.data.message
      : error.message
    return Promise.reject(new Error(message || '请求失败'))
  }
)

export function login(payload) {
  return request.post('auth/login', payload)
}

export function createQr(payload) {
  return request.post('qrcodes', payload)
}

export function updateQr(code, payload) {
  return request.put(`qrcodes/${code}`, payload)
}

export function updateQrEnabled(code, enabled) {
  return request.put(`qrcodes/${code}/enabled?enabled=${enabled}`)
}

export function listQr() {
  return request.get('qrcodes')
}

export function getStats(code) {
  return request.get(`qrcodes/${code}/stats`)
}


export function logoutRequest() {
  return request.post('auth/logout')
}

export function deleteQr(code) {
  return request.delete(`qrcodes/${code}`)
}

export function batchDeleteQr(codes) {
  return request.post('qrcodes/batch-delete', codes)
}

export function searchQr(keyword, enabled) {
  const params = new URLSearchParams()
  if (keyword) params.append('keyword', keyword)
  if (enabled !== null && enabled !== undefined) params.append('enabled', enabled)
  return request.get(`qrcodes/search?${params.toString()}`)
}

export function getSummary() {
  return request.get('qrcodes/summary')
}

export function getRefererStats(code) {
  const url = code ? `visits/referer?code=${code}` : 'visits/referer'
  return request.get(url)
}

export function getHourStats(code) {
  const url = code ? `visits/hour?code=${code}` : 'visits/hour'
  return request.get(url)
}

export function getDeviceStats(code) {
  const url = code ? `visits/device?code=${code}` : 'visits/device'
  return request.get(url)
}

export function getBrowserStats(code) {
  const url = code ? `visits/browser?code=${code}` : 'visits/browser'
  return request.get(url)
}

export function getLocationStats(code) {
  const url = code ? `visits/location?code=${code}` : 'visits/location'
  return request.get(url)
}

export function checkApiStatus() {
  return request.get('status')
}
