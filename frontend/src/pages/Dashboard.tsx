import { useEffect, useState } from 'react'
import { getRecentEvents, subscribeToTimeline, TimelineEvent } from '../api/client'
import Timeline from '../components/Timeline'

export default function Dashboard() {
  const [events, setEvents] = useState<TimelineEvent[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // 1. Subscribe immediately to catch live events
    const unsubscribe = subscribeToTimeline((event) => {
      setEvents((prev) => {
        // Simple dedupe check
        if (prev.some((e) => e.id === event.id)) return prev
        return [event, ...prev]
      })
    })

    // 2. Fetch recent history
    getRecentEvents()
      .then((recentEvents) => {
        setEvents((prev) => {
          // Merge: Streamed events (prev) + Historical (recentEvents)
          // Streamed events are likely newer, so they come first.
          const allEvents = [...prev, ...recentEvents]

          // Deduplicate by Content Signature (to handle backend re-ingestion duplicates)
          const seenIds = new Set()
          const seenContent = new Set()
          const uniqueEvents: TimelineEvent[] = []

          for (const evt of allEvents) {
            // 1. ID Check
            if (seenIds.has(evt.id)) continue

            // 2. Content Check
            // Generate a deterministic signature for the event content
            // For News: Title matches are duplicates
            // For Market/Weather: Identical payload is duplicate
            const payloadStr = JSON.stringify(evt.payload || {})
            const contentKey = `${evt.eventType}:${payloadStr}`

            if (seenContent.has(contentKey)) continue

            seenIds.add(evt.id)
            seenContent.add(contentKey)
            uniqueEvents.push(evt)
          }

          // Sort by timestamp descending to be safe
          return uniqueEvents.sort((a, b) =>
            new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
          )
        })
        setLoading(false)
      })
      .catch((error) => {
        console.error('Failed to load recent events:', error)
        setLoading(false)
      })

    return () => unsubscribe()
  }, [])

  return (
    <div className="page">
      <h1 className="page-title">Dashboard</h1>
      <div className="card">
        <h2 style={{ marginBottom: '1rem', color: '#00bcd4' }}>Live Timeline</h2>
        {loading ? (
          <p>Loading events...</p>
        ) : (
          <Timeline events={events} />
        )}
      </div>
      <div className="card">
        <h2 style={{ marginBottom: '1rem', color: '#00bcd4' }}>Charts Placeholder</h2>
        <div style={{ padding: '2rem', textAlign: 'center', color: '#777' }}>
          <p>Market charts and visualizations will be displayed here</p>
        </div>
      </div>
    </div>
  )
}
