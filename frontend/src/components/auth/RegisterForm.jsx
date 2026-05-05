import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function RegisterForm() {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: ''
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  function handleChange(e) {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }))
    setError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (formData.password.length < 8) {
      setError('Lösenordet måste vara minst 8 tecken')
      return
    }

    setLoading(true)
    try {
      const data = await register(formData)
      setSuccess(data.message)
      setTimeout(() => navigate('/login'), 3000)
    } catch (err) {
      const fieldErrors = err.response?.data?.fieldErrors
      if (fieldErrors) {
        setError(Object.values(fieldErrors).join(', '))
      } else {
        setError(err.response?.data?.message || 'Registrering misslyckades')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-card">
      <h1 className="auth-title">Skapa konto</h1>
      <p className="auth-subtitle">SCB Flyttningsstatistik</p>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="firstName">Förnamn</label>
            <input
              id="firstName"
              name="firstName"
              type="text"
              value={formData.firstName}
              onChange={handleChange}
              placeholder="Anna"
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="lastName">Efternamn</label>
            <input
              id="lastName"
              name="lastName"
              type="text"
              value={formData.lastName}
              onChange={handleChange}
              placeholder="Svensson"
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="email">E-post</label>
          <input
            id="email"
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="din@epost.se"
            required
            autoComplete="email"
          />
        </div>

        <div className="form-group">
          <label htmlFor="password">Lösenord</label>
          <input
            id="password"
            name="password"
            type="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Minst 8 tecken"
            required
            autoComplete="new-password"
          />
        </div>

        <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
          {loading ? 'Skapar konto...' : 'Skapa konto'}
        </button>
      </form>

      <p className="auth-footer">
        Har du redan ett konto?{' '}
        <Link to="/login">Logga in</Link>
      </p>
    </div>
  )
}
