import { useEffect, useState } from 'react'
import Spinner from '../components/layout/Spinner'
import '../styles/dashboard.css'

function MethodBadge({ method }) {
  const cls = `method-badge method-${method.toLowerCase()}`
  return <span className={cls}>{method}</span>
}

function EndpointCard({ ep }) {
  const hasParams = Object.keys(ep.params).length > 0

  return (
    <div className="endpoint-card">
      <div className="endpoint-header">
        <MethodBadge method={ep.method} />
        <span className="endpoint-path">{ep.path}</span>
        <span className="endpoint-role">{ep.requiredRole}</span>
      </div>
      <div className="endpoint-body">
        <p className="endpoint-description">{ep.description}</p>

        {hasParams && (
          <div>
            <p className="endpoint-section-title">Parametrar</p>
            <div className="params-list">
              {Object.entries(ep.params).map(([key, desc]) => (
                <div key={key} className="param-row">
                  <span className="param-key">{key}</span>
                  <span className="param-desc">{desc}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        <div>
          <p className="endpoint-section-title">Exempelsvar</p>
          <pre className="example-block">{ep.exampleResponse}</pre>
        </div>
      </div>
    </div>
  )
}

export default function DocsPage() {
  const [docs, setDocs] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('/api/docs')
      .then(res => {
        if (!res.ok) throw new Error('Kunde inte hämta dokumentation')
        return res.json()
      })
      .then(setDocs)
      .catch(err => setError(err.message))
  }, [])

  if (error) {
    return (
      <div className="docs-page">
        <div className="alert alert-error">{error}</div>
      </div>
    )
  }

  if (!docs) {
    return (
      <div className="docs-page">
        <Spinner text="Hämtar dokumentation..." />
      </div>
    )
  }

  return (
    <div className="docs-page">
      <div className="docs-header">
        <h1>{docs.title}</h1>
        <div className="docs-meta">
          <span>Version {docs.version}</span>
          <span>Base URL: <code>{docs.baseUrl}</code></span>
        </div>
      </div>

      {docs.endpoints.map(ep => (
        <EndpointCard key={`${ep.method}-${ep.path}`} ep={ep} />
      ))}
    </div>
  )
}
