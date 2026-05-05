export default function Spinner({ text = 'Laddar...' }) {
  return (
    <div className="spinner-wrapper">
      <div className="spinner" role="status" aria-label={text} />
      <p className="spinner-text">{text}</p>
    </div>
  )
}
