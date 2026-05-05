import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">SCB Flyttningar</Link>
      </div>
      <div className="navbar-links">
        {user ? (
          <>
            <span className="navbar-user">
              {user.firstName} &nbsp;
              <span className="badge">{isAdmin ? 'Admin' : 'Användare'}</span>
            </span>
            {isAdmin && (
              <Link to="/admin" className="nav-link">Adminpanel</Link>
            )}
            <Link to="/dashboard" className="nav-link">Dashboard</Link>
            <button onClick={handleLogout} className="btn btn-outline">
              Logga ut
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-link">Logga in</Link>
            <Link to="/register" className="btn btn-primary">Registrera</Link>
          </>
        )}
      </div>
    </nav>
  )
}
