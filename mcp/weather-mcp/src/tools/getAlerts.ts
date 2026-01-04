interface GetAlertsParams {
  latitude: number
  longitude: number
}

export async function getAlerts(params: GetAlertsParams, correlationId?: string) {
  const { latitude, longitude } = params

  if (latitude === undefined || longitude === undefined) {
    throw new Error('latitude and longitude are required')
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'weather-mcp',
      eventType: 'weather_alerts_start',
      message: 'Fetching weather alerts',
      correlationId,
      data: { latitude, longitude },
    })
  )

  const mockAlerts = [
    {
      id: '1',
      event: 'Thunderstorm Warning',
      severity: 'moderate',
      onset: new Date().toISOString(),
      expires: new Date(Date.now() + 3600000).toISOString(),
      description: 'Thunderstorm warning in effect',
    },
  ]

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'weather-mcp',
      eventType: 'weather_alerts_success',
      message: 'Weather alerts fetched',
      correlationId,
      data: { latitude, longitude, alertCount: mockAlerts.length },
    })
  )

  return {
    latitude,
    longitude,
    alerts: mockAlerts,
    timestamp: new Date().toISOString(),
    note: 'Mock alerts - real implementation would use weather service API',
  }
}
