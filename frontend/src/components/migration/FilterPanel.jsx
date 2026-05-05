import { useState, useEffect } from 'react'
import { migrationService } from '../../services/migrationService'

export default function FilterPanel({ onFilter }) {
  const [options, setOptions] = useState({ regions: [], ageGroups: [], years: [] })
  const [filters, setFilters] = useState({
    region: '',
    gender: '',
    ageGroup: '',
    year: ''
  })

  useEffect(() => {
    migrationService.getFilterOptions().then(setOptions).catch(console.error)
  }, [])

  function handleChange(e) {
    setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    onFilter(filters)
  }

  function handleReset() {
    const empty = { region: '', gender: '', ageGroup: '', year: '' }
    setFilters(empty)
    onFilter(empty)
  }

  return (
    <div className="filter-panel">
      <h2 className="filter-title">Filtrera data</h2>
      <form onSubmit={handleSubmit} className="filter-form">
        <div className="filter-grid">
          <div className="form-group">
            <label htmlFor="region">Region</label>
            <select id="region" name="region" value={filters.region} onChange={handleChange}>
              <option value="">Alla regioner</option>
              {options.regions.map(r => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="gender">Kön</label>
            <select id="gender" name="gender" value={filters.gender} onChange={handleChange}>
              <option value="">Båda könen</option>
              <option value="men">Män</option>
              <option value="women">Kvinnor</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="ageGroup">Åldersgrupp</label>
            <select id="ageGroup" name="ageGroup" value={filters.ageGroup} onChange={handleChange}>
              <option value="">Alla åldrar</option>
              {options.ageGroups.map(a => (
                <option key={a} value={a}>{a} år</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="year">År</label>
            <select id="year" name="year" value={filters.year} onChange={handleChange}>
              <option value="">Alla år</option>
              {options.years.map(y => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="filter-actions">
          <button type="submit" className="btn btn-primary">Sök</button>
          <button type="button" onClick={handleReset} className="btn btn-outline">
            Återställ
          </button>
        </div>
      </form>
    </div>
  )
}
