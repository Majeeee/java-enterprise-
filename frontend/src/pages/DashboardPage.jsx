import { useState } from 'react'
import FilterPanel from '../components/migration/FilterPanel'
import DataTable from '../components/migration/DataTable'
import { migrationService } from '../services/migrationService'
import { useAuth } from '../context/AuthContext'
import '../styles/dashboard.css'

export default function DashboardPage() {
  const { user } = useAuth()
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)

  async function handleFilter(filters) {
    setLoading(true)
    setHasSearched(true)
    try {
      const result = await migrationService.getMigrations(filters)
      setData(result)
    } catch (err) {
      console.error('Kunde inte hämta data:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <h1>SCB Flyttningsstatistik</h1>
        <p>Välkommen, {user?.firstName}. Filtrera och utforska flyttningar 1997–2024.</p>
      </div>

      <FilterPanel onFilter={handleFilter} />

      {hasSearched && (
        <DataTable data={data} loading={loading} />
      )}

      {!hasSearched && (
        <div className="dashboard-intro">
          <p>Välj filter ovan och klicka <strong>Sök</strong> för att hämta statistik.</p>
        </div>
      )}
    </div>
  )
}
