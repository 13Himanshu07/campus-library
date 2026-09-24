import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../services/api'
const AuthContext = createContext(null)
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => { try { return JSON.parse(sessionStorage.getItem('library_user')) } catch { return null } })
  const [ready, setReady] = useState(false)
  useEffect(() => { const expired = () => { setUser(null); sessionStorage.removeItem('library_user') }; window.addEventListener('library:expired', expired); if (sessionStorage.getItem('library_token')) api.get('/auth/me').then(r => { setUser(r.data); sessionStorage.setItem('library_user', JSON.stringify(r.data)) }).catch(expired).finally(() => setReady(true)); else setReady(true); return () => window.removeEventListener('library:expired', expired) }, [])
  const login = async credentials => { const { data } = await api.post('/auth/login', credentials); sessionStorage.setItem('library_token', data.token); sessionStorage.setItem('library_user', JSON.stringify(data.user)); setUser(data.user); return data.user }
  const register = async input => api.post('/auth/register', input)
  const logout = () => { sessionStorage.removeItem('library_token'); sessionStorage.removeItem('library_user'); setUser(null) }
  return <AuthContext.Provider value={{ user, ready, login, logout, register, setUser }}>{children}</AuthContext.Provider>
}
export const useAuth = () => useContext(AuthContext)
