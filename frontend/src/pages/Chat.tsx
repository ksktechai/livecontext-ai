import { useState } from 'react'
import { sendChatMessage, ChatResponse } from '../api/client'
import EvidenceList from '../components/EvidenceList'

export default function Chat() {
  const [question, setQuestion] = useState('')
  const [response, setResponse] = useState<ChatResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!question.trim()) return

    setLoading(true)
    setError(null)

    try {
      const result = await sendChatMessage(question)
      setResponse(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send message')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1 className="page-title">Chat</h1>

      <div className="card">
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <textarea
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask a question about market data, news, or weather..."
            rows={4}
            disabled={loading}
          />
          <button type="submit" className="button" disabled={loading}>
            {loading ? 'Processing...' : 'Send Question'}
          </button>
        </form>
      </div>

      {error && (
        <div className="card" style={{ borderColor: '#f44336' }}>
          <p style={{ color: '#f44336' }}>Error: {error}</p>
        </div>
      )}

      {response && (
        <>
          <div className="card">
            <h2 style={{ marginBottom: '1rem', color: '#00bcd4' }}>Answer</h2>
            <p style={{ lineHeight: 1.6 }}>{response.answer}</p>
            {response.correlationId && (
              <p style={{ marginTop: '1rem', fontSize: '0.875rem', color: '#777' }}>
                Correlation ID: {response.correlationId}
              </p>
            )}
          </div>

          {response.evidence && response.evidence.length > 0 && (
            <div className="card">
              <h2 style={{ marginBottom: '1rem', color: '#00bcd4' }}>Evidence</h2>
              <EvidenceList evidence={response.evidence} />
            </div>
          )}
        </>
      )}
    </div>
  )
}
