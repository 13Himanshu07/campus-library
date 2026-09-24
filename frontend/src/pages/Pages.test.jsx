import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router-dom'
import { Login } from './Pages'

const { loginMock } = vi.hoisted(() => ({ loginMock: vi.fn() }))
vi.mock('../context/AuthContext', () => ({ useAuth: () => ({ login: loginMock }) }))

function Destination(){const location=useLocation();return <div>Route: {location.pathname}</div>}
afterEach(()=>{cleanup();loginMock.mockReset()})

describe('member sign-in flow',()=>{
 it('submits credentials and routes a member to their dashboard',async()=>{
  loginMock.mockResolvedValue({role:'MEMBER'})
  const user=userEvent.setup()
  render(<MemoryRouter initialEntries={['/login']}><Routes><Route path="/login" element={<Login/>}/><Route path="/member/dashboard" element={<Destination/>}/></Routes></MemoryRouter>)
  await user.type(screen.getByLabelText(/email address/i),'reader@college.edu')
  await user.type(screen.getByLabelText(/^password$/i),'BookwormPass9!')
  await user.click(screen.getByRole('button',{name:/sign in/i}))
  expect(await screen.findByText('Route: /member/dashboard')).toBeTruthy()
  expect(loginMock).toHaveBeenCalledWith({email:'reader@college.edu',password:'BookwormPass9!'})
 })
})
