import { TimelineEvent } from '../api/client'

interface TimelineProps {
  events: TimelineEvent[]
}

export default function Timeline({ events }: TimelineProps) {
  if (events.length === 0) {
    return <p style={{ color: '#777' }}>No timeline events yet</p>
  }

  const renderPayload = (event: TimelineEvent) => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    let payload = (event.payload || {}) as any

    // Fix: Handle double-serialized JSON (payload arriving as a string)
    if (typeof payload === 'string') {
      try {
        payload = JSON.parse(payload)
      } catch (e) {
        // keep as string if parse fails
        console.warn('Failed to parse payload string:', e)
      }
    }

    const type = event.eventType

    if (type.includes('market_fetch')) {
      if (payload.error) return <span style={{ color: '#ef5350' }}>Error: {payload.error} ({payload.symbol})</span>
      // Success
      if (payload.price) {
        return (
          <span>
            <span style={{ color: '#4caf50', fontWeight: 'bold' }}>{payload.symbol?.toUpperCase()}</span>
            <span style={{ margin: '0 0.5rem' }}>${payload.price}</span>
            <span style={{ color: '#777', fontSize: '0.8rem' }}>({payload.provider})</span>
          </span>
        )
      }
    }

    if (type.includes('news') || type.includes('rss')) {
      return (
        <div>
          <div style={{ fontWeight: 'bold', color: '#e0e0e0' }}>{payload.title}</div>
          <div style={{ fontSize: '0.8rem', color: '#999', marginTop: '0.2rem' }}>
            {payload.source || 'Unknown Source'}
            {payload.publishedAt && ` • ${new Date(payload.publishedAt).toLocaleDateString()}`}
          </div>
          {payload.link && (
            <div style={{ fontSize: '0.8rem', marginTop: '0.2rem' }}>
              <a href={payload.link} target="_blank" rel="noopener noreferrer" style={{ color: '#00bcd4', textDecoration: 'none' }}>Read more</a>
            </div>
          )}
        </div>
      )
    }

    if (type.includes('weather')) {
      if (payload.error) return <span style={{ color: '#ef5350' }}>Weather Error: {payload.error}</span>

      // OpenMeteo formats can vary, handle simple case if known
      if (payload.hourly) {
        return <span>Weather data for {payload.latitude}, {payload.longitude}</span>
      }
      return <span>Weather check for {payload.latitude}, {payload.longitude}</span>
    }

    // Fallback: prettier JSON
    return (
      <pre style={{ margin: 0, whiteSpace: 'pre-wrap', fontFamily: 'monospace', fontSize: '0.75rem', color: '#888' }}>
        {JSON.stringify(payload, null, 2)}
      </pre>
    )
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
      {events.map((event) => (
        <div
          key={event.id}
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
              {event.eventType}
            </span>
            <span style={{ color: '#777', fontSize: '0.875rem' }}>
              {new Date(event.timestamp).toLocaleString()}
            </span>
          </div>
          <div style={{ fontSize: '0.875rem', color: '#ccc' }}>
            {renderPayload(event)}
          </div>
        </div>
      ))}
    </div>
  )
}
