import { useState } from 'react'
import FilterPanel from '../components/migration/FilterPanel'
import DataTable from '../components/migration/DataTable'
import StatsSummary from '../components/migration/StatsSummary'
import AnalysisChart from '../components/migration/AnalysisChart'
import Spinner from '../components/layout/Spinner'
import { migrationService } from '../services/migrationService'
import { useAuth } from '../context/AuthContext'
import '../styles/dashboard.css'

export default function DashboardPage() {
  const { user } = useAuth()
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)
  const [error, setError] = useState(null)

  async function handleFilter(filters) {
    setLoading(true)
    setHasSearched(true)
    setError(null)
    try {
      const result = await migrationService.getMigrations(filters)
      setData(result)
    } catch (err) {
      setError('Kunde inte hämta data. Försök igen.')
      setData([])
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

      {error && <div className="alert alert-error">{error}</div>}

      {loading && <Spinner text="Hämtar statistik..." />}

      {!loading && hasSearched && data.length > 0 && (
        <>
          <StatsSummary data={data} />
          <AnalysisChart data={data} />
          <DataTable data={data} />
        </>
      )}

      {!loading && hasSearched && data.length === 0 && !error && (
        <div className="empty-state">
          <p>Inga resultat hittades. Prova att justera filtren.</p>
        </div>
      )}

      {!hasSearched && (
        <div className="dashboard-intro">
          <p>Välj filter ovan och klicka <strong>Sök</strong> för att hämta statistik.</p>
        </div>
      )}
    </div>
  )
}
