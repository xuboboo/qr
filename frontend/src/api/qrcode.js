import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
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
    const message = error.response && error.response.data && error.response.data.message
      ? error.response.data.message
      : error.message
    return Promise.reject(new Error(message || '请求失败'))
  }
)

export function createQr(payload) {
  return request.post('/qrcodes', payload)
}

export function updateQr(code, payload) {
  return request.put(`/qrcodes/${code}`, payload)
}

export function updateQrEnabled(code, enabled) {
  return request.put(`/qrcodes/${code}/enabled?enabled=${enabled}`)
}

export function listQr() {
  return request.get('/qrcodes')
}

export function getStats(code) {
  return request.get(`/qrcodes/${code}/stats`)
}
