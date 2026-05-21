import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8079/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const stored = localStorage.getItem('dcx_auth')
  if (stored) {
    const { token } = JSON.parse(stored)
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || ''
    const isAuthEndpoint = url.includes('/v1/auth/')
    // Only auto-redirect on 401 for non-auth endpoints
    // Auth endpoints (login/signup) handle their own errors
    if (err.response?.status === 401 && !isAuthEndpoint) {
      localStorage.removeItem('dcx_auth')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
