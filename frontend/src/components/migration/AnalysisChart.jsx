function aggregateByYear(data) {
  return Object.values(data.reduce((acc, row) => {
    const key = row.year
    if (!acc[key]) {
      acc[key] = { label: String(row.year), immigrations: 0, emigrations: 0, net: 0 }
    }
    acc[key].immigrations += row.immigrations
    acc[key].emigrations += row.emigrations
    acc[key].net += row.netMigration
    return acc
  }, {})).sort((a, b) => Number(a.label) - Number(b.label))
}

function aggregateByRegion(data) {
  return Object.values(data.reduce((acc, row) => {
    const key = row.region
    if (!acc[key]) {
      acc[key] = { label: row.region, immigrations: 0, emigrations: 0, net: 0 }
    }
    acc[key].immigrations += row.immigrations
    acc[key].emigrations += row.emigrations
    acc[key].net += row.netMigration
    return acc
  }, {}))
    .sort((a, b) => Math.abs(b.net) - Math.abs(a.net))
    .slice(0, 8)
}

function formatNumber(value) {
  return value.toLocaleString('sv-SE')
}

function BarChart({ rows }) {
  const maxValue = Math.max(...rows.map(row => Math.max(row.immigrations, row.emigrations)), 1)

  return (
    <div className="analysis-bars">
      {rows.map(row => {
        const immigrationsWidth = `${Math.max((row.immigrations / maxValue) * 100, 2)}%`
        const emigrationsWidth = `${Math.max((row.emigrations / maxValue) * 100, 2)}%`

        return (
          <div className="analysis-row" key={row.label}>
            <div className="analysis-row-label" title={row.label}>{row.label}</div>
            <div className="analysis-row-bars">
              <div className="analysis-bar-line">
                <span className="analysis-bar analysis-bar-in" style={{ width: immigrationsWidth }} />
                <span className="analysis-value">{formatNumber(row.immigrations)}</span>
              </div>
              <div className="analysis-bar-line">
                <span className="analysis-bar analysis-bar-out" style={{ width: emigrationsWidth }} />
                <span className="analysis-value">{formatNumber(row.emigrations)}</span>
              </div>
            </div>
            <div className={`analysis-net ${row.net >= 0 ? 'positive' : 'negative'}`}>
              {row.net >= 0 ? '+' : ''}{formatNumber(row.net)}
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default function AnalysisChart({ data }) {
  const byYear = aggregateByYear(data)
  const byRegion = aggregateByRegion(data)
  const chartRows = byYear.length > 1 ? byYear : byRegion
  const title = byYear.length > 1 ? 'Utveckling per ar' : 'Regioner med storst nettoflytt'

  return (
    <section className="analysis-panel">
      <div className="analysis-header">
        <div>
          <h2>Analys</h2>
          <p>{title}</p>
        </div>
        <div className="analysis-legend">
          <span><i className="legend-dot legend-in" /> Inflyttade</span>
          <span><i className="legend-dot legend-out" /> Utflyttade</span>
          <span>Netto</span>
        </div>
      </div>

      <BarChart rows={chartRows} />
    </section>
  )
}
