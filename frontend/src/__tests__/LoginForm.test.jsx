import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import LoginForm from '../components/auth/LoginForm'
import { useAuth } from '../context/AuthContext'

vi.mock('../context/AuthContext')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

describe('LoginForm', () => {
  const mockLogin = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({
      login: mockLogin,
      user: null,
      logout: vi.fn(),
      register: vi.fn(),
      isAdmin: false,
    })
  })

  function renderForm() {
    return render(
      <MemoryRouter>
        <LoginForm />
      </MemoryRouter>
    )
  }

  it('renders email and password fields with a submit button', () => {
    renderForm()
    expect(screen.getByLabelText(/e-post/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/lösenord/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /logga in/i })).toBeInTheDocument()
  })

  it('shows error message when login fails', async () => {
    mockLogin.mockRejectedValue({
      response: { data: { message: 'Felaktig e-post eller lösenord' } },
    })

    renderForm()
    fireEvent.change(screen.getByLabelText(/e-post/i), {
      target: { value: 'bad@test.se' },
    })
    fireEvent.change(screen.getByLabelText(/lösenord/i), {
      target: { value: 'wrongpass' },
    })
    fireEvent.click(screen.getByRole('button', { name: /logga in/i }))

    await waitFor(() => {
      expect(
        screen.getByText('Felaktig e-post eller lösenord')
      ).toBeInTheDocument()
    })
  })

  it('shows fallback error when response has no message', async () => {
    mockLogin.mockRejectedValue(new Error('Network error'))

    renderForm()
    fireEvent.click(screen.getByRole('button', { name: /logga in/i }))

    await waitFor(() => {
      expect(screen.getByText('Inloggning misslyckades')).toBeInTheDocument()
    })
  })

  it('navigates to /dashboard on successful login', async () => {
    mockLogin.mockResolvedValue({
      email: 'user@test.se',
      role: 'ROLE_USER',
      firstName: 'Test',
    })

    renderForm()
    fireEvent.change(screen.getByLabelText(/e-post/i), {
      target: { value: 'user@test.se' },
    })
    fireEvent.change(screen.getByLabelText(/lösenord/i), {
      target: { value: 'correct123' },
    })
    fireEvent.click(screen.getByRole('button', { name: /logga in/i }))

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
    })
  })

  it('disables the button while loading', async () => {
    mockLogin.mockImplementation(
      () => new Promise(resolve => setTimeout(resolve, 100))
    )

    renderForm()
    fireEvent.click(screen.getByRole('button', { name: /logga in/i }))

    expect(screen.getByRole('button', { name: /loggar in/i })).toBeDisabled()
  })
})
