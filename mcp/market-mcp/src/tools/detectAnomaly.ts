interface DetectAnomalyParams {
  symbol: string
  threshold?: number
}

export async function detectAnomaly(params: DetectAnomalyParams, correlationId?: string) {
  const { symbol, threshold = 2.0 } = params

  if (!symbol) {
    throw new Error('symbol is required')
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'market-mcp',
      eventType: 'anomaly_detect_start',
      message: 'Detecting market anomalies',
      correlationId,
      data: { symbol, threshold },
    })
  )

  const mockAnomalies = [
    {
      date: new Date(Date.now() - 86400000).toISOString(),
      type: 'volume_spike',
      severity: 'medium',
      description: 'Trading volume 3.2x above average',
    },
  ]

  const result = {
    symbol,
    anomalies: mockAnomalies,
    threshold,
    timestamp: new Date().toISOString(),
    note: 'Mock anomalies - real implementation would analyze historical patterns',
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'market-mcp',
      eventType: 'anomaly_detect_success',
      message: 'Anomaly detection completed',
      correlationId,
      data: { symbol, anomalyCount: mockAnomalies.length },
    })
  )

  return result
}
