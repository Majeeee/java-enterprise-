export default function DataTable({ data, loading }) {
  if (loading) {
    return <div className="loading-state">Hämtar data...</div>
  }

  if (!data || data.length === 0) {
    return (
      <div className="empty-state">
        <p>Inga resultat hittades. Prova att justera filtren.</p>
      </div>
    )
  }

  return (
    <div className="table-wrapper">
      <p className="table-count">{data.length} rader</p>
      <div className="table-scroll">
        <table className="data-table">
          <thead>
            <tr>
              <th>Region</th>
              <th>Kön</th>
              <th>Åldersgrupp</th>
              <th>År</th>
              <th>Inflyttade</th>
              <th>Utflyttade</th>
              <th>Nettoflytt</th>
            </tr>
          </thead>
          <tbody>
            {data.map(row => (
              <tr key={row.id}>
                <td>{row.region}</td>
                <td>{row.gender === 'men' ? 'Män' : 'Kvinnor'}</td>
                <td>{row.ageGroup} år</td>
                <td>{row.year}</td>
                <td className="num-cell">{row.immigrations.toLocaleString('sv-SE')}</td>
                <td className="num-cell">{row.emigrations.toLocaleString('sv-SE')}</td>
                <td className={`num-cell ${row.netMigration >= 0 ? 'positive' : 'negative'}`}>
                  {row.netMigration >= 0 ? '+' : ''}{row.netMigration.toLocaleString('sv-SE')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
