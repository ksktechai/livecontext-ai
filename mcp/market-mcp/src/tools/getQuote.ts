import fetch from 'node-fetch'

interface GetQuoteParams {
  symbol: string
  interval?: string
}

export async function getQuote(params: GetQuoteParams, correlationId?: string) {
  const startTime = Date.now()
  const { symbol, interval = 'daily' } = params

  if (!symbol) {
    throw new Error('symbol is required')
  }

  const normalizedSymbol = symbol.toLowerCase()
  const intervalMap: Record<string, string> = {
    daily: 'd',
    d: 'd',
    weekly: 'w',
    w: 'w',
    monthly: 'm',
    m: 'm',
  }
  const normalizedInterval = intervalMap[interval.toLowerCase()] || 'd'

  const url = `https://stooq.com/q/d/l/?s=${normalizedSymbol}&i=${normalizedInterval}`

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'market-mcp',
      eventType: 'market_fetch_start',
      message: 'Fetching market data from Stooq',
      correlationId,
      data: { provider: 'Stooq', symbol: normalizedSymbol, interval: normalizedInterval, url },
    })
  )

  try {
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`Stooq API error: ${response.statusText}`)
    }

    const csvData = await response.text()

    if (csvData.includes('Exceeded the daily hits limit')) {
      throw new Error('Stooq API rate limit exceeded')
    }

    const lines = csvData.trim().split('\n')

    if (lines.length < 2) {
      throw new Error('No data available for symbol (empty response)')
    }

    const headers = lines[0].split(',')
    const latestData = lines[lines.length - 1].split(',')

    const quote: Record<string, any> = {}
    headers.forEach((header, index) => {
      quote[header.toLowerCase()] = latestData[index]
    })

    const duration = Date.now() - startTime

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'market-mcp',
        eventType: 'market_fetch_success',
        message: 'Successfully fetched market data',
        correlationId,
        data: { provider: 'Stooq', symbol: normalizedSymbol, duration_ms: duration },
      })
    )

    return {
      symbol,
      quote,
      timestamp: new Date().toISOString(),
      provider: 'Stooq',
    }
  } catch (error: any) {
    const duration = Date.now() - startTime

    console.error(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'ERROR',
        service: 'market-mcp',
        eventType: 'market_fetch_error',
        message: 'Failed to fetch market data',
        correlationId,
        data: { provider: 'Stooq', symbol: normalizedSymbol, error: error.message, duration_ms: duration },
      })
    )

    throw error
  }
}
