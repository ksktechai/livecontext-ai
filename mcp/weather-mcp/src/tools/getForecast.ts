import fetch from 'node-fetch'

interface GetForecastParams {
  latitude: number
  longitude: number
}

export async function getForecast(params: GetForecastParams, correlationId?: string) {
  const startTime = Date.now()
  const { latitude, longitude } = params

  if (latitude === undefined || longitude === undefined) {
    throw new Error('latitude and longitude are required')
  }

  const url = `https://api.open-meteo.com/v1/forecast?latitude=${latitude}&longitude=${longitude}&hourly=temperature_2m,precipitation&forecast_days=1`

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'weather-mcp',
      eventType: 'weather_fetch_start',
      message: 'Fetching weather forecast',
      correlationId,
      data: { provider: 'Open-Meteo', latitude, longitude, url },
    })
  )

  try {
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`Open-Meteo API error: ${response.statusText}`)
    }

    const data: any = await response.json()

    const hourly = data.hourly.time.slice(0, 24).map((time: string, index: number) => ({
      time,
      temperature: data.hourly.temperature_2m[index],
      precipitation: data.hourly.precipitation[index],
    }))

    const duration = Date.now() - startTime

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'weather-mcp',
        eventType: 'weather_fetch_success',
        message: 'Successfully fetched weather forecast',
        correlationId,
        data: { provider: 'Open-Meteo', latitude, longitude, duration_ms: duration },
      })
    )

    return {
      latitude,
      longitude,
      hourly,
      timestamp: new Date().toISOString(),
      provider: 'Open-Meteo',
    }
  } catch (error: any) {
    const duration = Date.now() - startTime

    console.error(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'ERROR',
        service: 'weather-mcp',
        eventType: 'weather_fetch_error',
        message: 'Failed to fetch weather forecast',
        correlationId,
        data: { provider: 'Open-Meteo', latitude, longitude, error: error.message, duration_ms: duration },
      })
    )

    throw error
  }
}
