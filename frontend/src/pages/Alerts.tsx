import { useState, useEffect } from 'react'
import { createAlert, getAlerts, AlertRule } from '../api/client'

export default function Alerts() {
  const [alerts, setAlerts] = useState<AlertRule[]>([])
  const [newAlertName, setNewAlertName] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadAlerts()
  }, [])

  const loadAlerts = async () => {
    try {
      const data = await getAlerts()
      setAlerts(data)
    } catch (error) {
      console.error('Failed to load alerts:', error)
    }
  }

  const handleCreateAlert = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newAlertName.trim()) return

    setLoading(true)
    try {
      const newAlert: AlertRule = {
        name: newAlertName,
        condition: { type: 'placeholder', value: 'Sample condition' },
        enabled: true,
      }

      await createAlert(newAlert)
      setNewAlertName('')
      await loadAlerts()
    } catch (error) {
      console.error('Failed to create alert:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Alerts</h1>

      <div className="card">
        <h2 style={{ marginBottom: '1rem', color: '#00bcd4' }}>Create New Alert</h2>
        <form onSubmit={handleCreateAlert} style={{ display: 'flex', gap: '1rem' }}>
          <input
            type="text"
            value={newAlertName}
            onChange={(e) => setNewAlertName(e.target.value)}
            placeholder="Alert name (e.g., 'AAPL price above $200')"
            disabled={loading}
          />
          <button type="submit" className="button" disabled={loading}>
            {loading ? 'Creating...' : 'Create Alert'}
          </button>
        </form>
      </div>

      <div className="card">
        <h2 style={{ marginBottom: '1rem', color: '#00bcd4' }}>Alert Rules</h2>
        {alerts.length === 0 ? (
          <p style={{ color: '#777' }}>No alerts configured yet</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {alerts.map((alert) => (
              <div
                key={alert.id}
                style={{
                  padding: '1rem',
                  background: '#0a0a0a',
                  border: '1px solid #333',
                  borderRadius: '4px',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: 'bold' }}>{alert.name}</span>
                  <span style={{ color: alert.enabled ? '#4caf50' : '#777' }}>
                    {alert.enabled ? 'Enabled' : 'Disabled'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
