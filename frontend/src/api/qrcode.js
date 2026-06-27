import axios from 'axios'

const TOKEN_KEY = 'qr_stat_admin_token'
const USERNAME_KEY = 'qr_stat_admin_username'

const isProd = process.env.NODE_ENV === 'production'

// 生产环境：页面在 /qr/ 下，用相对路径 api/xxx 自动适配 /qr 前缀
// 开发环境：用代理 /api → localhost:8080
const API_PREFIX = isProd ? 'api' : '/api'

const request = axios.create({
  baseURL: API_PREFIX,
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
