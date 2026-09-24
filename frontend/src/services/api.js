import axios from 'axios'
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api', timeout: 15000 })
api.interceptors.request.use(config => { const token = sessionStorage.getItem('library_token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config })
api.interceptors.response.use(r => r, error => { if (error.response?.status === 401) { sessionStorage.removeItem('library_token'); sessionStorage.removeItem('library_user'); window.dispatchEvent(new Event('library:expired')) } return Promise.reject(error) })
export const messageOf = e => e?.response?.data?.message || (e?.code === 'ECONNABORTED' ? 'The server took too long to respond.' : 'Could not connect to the library service. Check that the backend is running.')
