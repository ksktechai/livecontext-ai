import { Evidence } from '../api/client'

interface EvidenceListProps {
  evidence: Evidence[]
}

export default function EvidenceList({ evidence }: EvidenceListProps) {
  if (evidence.length === 0) {
    return <p style={{ color: '#777' }}>No evidence available</p>
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      {evidence.map((item, index) => (
        <div
          key={index}
          style={{
            padding: '1rem',
            background: '#0a0a0a',
            border: '1px solid #333',
            borderRadius: '4px',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span
              style={{
                color: '#00bcd4',
                fontWeight: 'bold',
                textTransform: 'uppercase',
                fontSize: '0.875rem',
              }}
            >
              {item.type}
            </span>
            <span style={{ color: '#777', fontSize: '0.875rem' }}>
              {new Date(item.timestamp).toLocaleString()}
            </span>
          </div>
          <div style={{ marginBottom: '0.5rem', color: '#e0e0e0' }}>
            <strong>Source:</strong> {item.source}
          </div>
          <div style={{ fontSize: '0.875rem', color: '#ccc' }}>{item.summary}</div>
        </div>
      ))}
    </div>
  )
}
