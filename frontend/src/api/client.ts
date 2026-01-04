const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export interface ChatRequest {
  question: string
  correlationId?: string
}

export interface Evidence {
  type: string
  source: string
  timestamp: string
  summary: string
}

export interface ChatResponse {
  answer: string
  evidence: Evidence[]
  correlationId: string
}

export interface TimelineEvent {
  id: number
  eventType: string
  timestamp: string
  payload: any
  sources?: any
}

export interface AlertRule {
  id?: number
  name: string
  condition: any
  enabled: boolean
}

function generateCorrelationId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

async function fetchWithCorrelationId(url: string, options: RequestInit = {}): Promise<Response> {
  const correlationId = generateCorrelationId()
  const headers = {
    'Content-Type': 'application/json',
    'X-Correlation-Id': correlationId,
    ...options.headers,
  }

  console.log(`[${correlationId}] Request: ${options.method || 'GET'} ${url}`)

  const response = await fetch(url, {
    ...options,
    headers,
  })

  console.log(`[${correlationId}] Response: ${response.status}`)

  return response
}

export async function sendChatMessage(question: string): Promise<ChatResponse> {
  const correlationId = generateCorrelationId()
  const response = await fetchWithCorrelationId(`${API_BASE_URL}/api/chat`, {
    method: 'POST',
    body: JSON.stringify({ question, correlationId }),
  })

  if (!response.ok) {
    throw new Error(`Chat request failed: ${response.statusText}`)
  }

  return response.json()
}

export function subscribeToTimeline(onEvent: (event: TimelineEvent) => void): () => void {
  const eventSource = new EventSource(`${API_BASE_URL}/api/stream`)

  eventSource.addEventListener('timeline', (e) => {
    const event: TimelineEvent = JSON.parse(e.data)
    onEvent(event)
  })

  eventSource.onerror = (error) => {
    console.error('SSE error:', error)
  }

  return () => eventSource.close()
}

export async function getRecentEvents(): Promise<TimelineEvent[]> {
  const response = await fetchWithCorrelationId(`${API_BASE_URL}/api/timeline/recent`)

  if (!response.ok) {
    throw new Error(`Failed to fetch recent events: ${response.statusText}`)
  }

  return response.json()
}

export async function createAlert(rule: AlertRule): Promise<AlertRule> {
  const response = await fetchWithCorrelationId(`${API_BASE_URL}/api/alerts`, {
    method: 'POST',
    body: JSON.stringify(rule),
  })

  if (!response.ok) {
    throw new Error(`Failed to create alert: ${response.statusText}`)
  }

  return response.json()
}

export async function getAlerts(): Promise<AlertRule[]> {
  const response = await fetchWithCorrelationId(`${API_BASE_URL}/api/alerts`)

  if (!response.ok) {
    throw new Error(`Failed to fetch alerts: ${response.statusText}`)
  }

  return response.json()
}
