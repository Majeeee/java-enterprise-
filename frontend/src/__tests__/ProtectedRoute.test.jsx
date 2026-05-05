import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../components/layout/ProtectedRoute'
import { useAuth } from '../context/AuthContext'

vi.mock('../context/AuthContext')

function renderWithRoutes(authValue, requireAdmin = false) {
  vi.mocked(useAuth).mockReturnValue(authValue)

  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route
          path="/protected"
          element={
            <ProtectedRoute requireAdmin={requireAdmin}>
              <div>Protected Content</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login Page</div>} />
        <Route path="/dashboard" element={<div>Dashboard</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => vi.clearAllMocks())

  it('redirects unauthenticated users to /login', () => {
    renderWithRoutes({ user: null, isAdmin: false })

    expect(screen.getByText('Login Page')).toBeInTheDocument()
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
  })

  it('renders children when user is authenticated', () => {
    renderWithRoutes({
      user: { email: 'u@test.se', firstName: 'Test' },
      isAdmin: false,
    })

    expect(screen.getByText('Protected Content')).toBeInTheDocument()
    expect(screen.queryByText('Login Page')).not.toBeInTheDocument()
  })

  it('redirects non-admin to /dashboard when requireAdmin is true', () => {
    renderWithRoutes(
      { user: { email: 'u@test.se', firstName: 'Test' }, isAdmin: false },
      true
    )

    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
  })

  it('renders children when user is admin and requireAdmin is true', () => {
    renderWithRoutes(
      { user: { email: 'admin@test.se', firstName: 'Admin' }, isAdmin: true },
      true
    )

    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })
})
