import { useState, useEffect } from 'react'
import { adminService } from '../services/adminService'
import '../styles/dashboard.css'

export default function AdminPage() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')

  useEffect(() => {
    fetchUsers()
  }, [])

  async function fetchUsers() {
    try {
      const data = await adminService.getAllUsers()
      setUsers(data)
    } catch (err) {
      console.error('Kunde inte hämta användare:', err)
    } finally {
      setLoading(false)
    }
  }

  async function handleEnable(userId) {
    try {
      await adminService.enableUser(userId)
      setMessage('Konto aktiverat')
      fetchUsers()
    } catch (err) {
      setMessage('Aktivering misslyckades')
    }
  }

  async function handleDelete(userId, email) {
    if (!window.confirm(`Ta bort kontot ${email}?`)) return
    try {
      await adminService.deleteUser(userId)
      setMessage('Konto borttaget')
      fetchUsers()
    } catch (err) {
      setMessage('Borttagning misslyckades')
    }
  }

  if (loading) return <div className="loading-state">Laddar användare...</div>

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <h1>Adminpanel</h1>
        <p>Hantera användarkonton</p>
      </div>

      {message && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          {message}
        </div>
      )}

      <div className="table-wrapper">
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>Namn</th>
                <th>E-post</th>
                <th>Roll</th>
                <th>Status</th>
                <th>Registrerad</th>
                <th>Åtgärder</th>
              </tr>
            </thead>
            <tbody>
              {users.map(user => (
                <tr key={user.id}>
                  <td>{user.firstName} {user.lastName}</td>
                  <td>{user.email}</td>
                  <td>
                    <span className={`badge ${user.role === 'ROLE_ADMIN' ? 'badge-admin' : ''}`}>
                      {user.role === 'ROLE_ADMIN' ? 'Admin' : 'Användare'}
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${user.enabled ? 'badge-active' : 'badge-inactive'}`}>
                      {user.enabled ? 'Aktiv' : 'Inaktiv'}
                    </span>
                  </td>
                  <td>{new Date(user.createdAt).toLocaleDateString('sv-SE')}</td>
                  <td className="action-cell">
                    {!user.enabled && (
                      <button
                        onClick={() => handleEnable(user.id)}
                        className="btn btn-sm btn-primary"
                      >
                        Aktivera
                      </button>
                    )}
                    {user.role !== 'ROLE_ADMIN' && (
                      <button
                        onClick={() => handleDelete(user.id, user.email)}
                        className="btn btn-sm btn-danger"
                      >
                        Ta bort
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
