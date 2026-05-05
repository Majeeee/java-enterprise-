export default function StatsSummary({ data }) {
  const totalImmigrations = data.reduce((sum, r) => sum + r.immigrations, 0)
  const totalEmigrations  = data.reduce((sum, r) => sum + r.emigrations, 0)
  const net               = totalImmigrations - totalEmigrations

  return (
    <div className="stats-bar">
      <div className="stat-item">
        <span className="stat-label">Träffar</span>
        <span className="stat-value">{data.length.toLocaleString('sv-SE')}</span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Inflyttade totalt</span>
        <span className="stat-value">{totalImmigrations.toLocaleString('sv-SE')}</span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Utflyttade totalt</span>
        <span className="stat-value">{totalEmigrations.toLocaleString('sv-SE')}</span>
      </div>
      <div className="stat-item">
        <span className="stat-label">Nettoflytt</span>
        <span className={`stat-value ${net >= 0 ? 'positive' : 'negative'}`}>
          {net >= 0 ? '+' : ''}{net.toLocaleString('sv-SE')}
        </span>
      </div>
    </div>
  )
}
