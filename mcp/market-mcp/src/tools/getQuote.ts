import fetch from 'node-fetch'
import { alphaVantageToolsCall } from '../alphaVantageMcpClient.js'

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

  const useAlphaVantage = !!process.env.ALPHAVANTAGE_API_KEY

  if (useAlphaVantage) {
    // AlphaVantage expects plain symbols (AAPL), not Stooq-style (AAPL.US)
    const avSymbol = symbol.toUpperCase().replace(/\.US$/i, '')

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'market-mcp',
        eventType: 'market_fetch_start',
        message: 'Fetching market data from Alpha Vantage MCP',
        correlationId,
        data: { provider: 'AlphaVantageMCP', symbol: avSymbol, tool: 'GLOBAL_QUOTE' },
      })
    )

    const mcpResp = await alphaVantageToolsCall(
      'GLOBAL_QUOTE',
      { symbol: avSymbol, datatype: 'csv' },
      correlationId
    )

    // Alpha Vantage MCP returns tool results wrapped; handle defensively
    const payload = (mcpResp?.result ?? mcpResp)?.content ?? (mcpResp?.result ?? mcpResp)

    // Alpha Vantage MCP often returns: content: [{ type: "text", text: "<csv>" }]
    const csvText: string | undefined =
      Array.isArray(payload) && payload.length > 0 && payload[0]?.type === 'text'
        ? String(payload[0]?.text ?? '')
        : undefined

    let quote: any = payload

    if (csvText && csvText.includes('symbol,') && csvText.includes('\n')) {
      const lines = csvText.trim().split(/\r?\n/)
      const headers = lines[0].split(',').map((h) => h.trim())
      const values = (lines[1] ?? '').split(',').map((v) => v.trim())

      const obj: Record<string, string> = {}
      headers.forEach((h, i) => {
        obj[h] = values[i] ?? ''
      })

      quote = obj
    }

    // Normalize output to your existing result shape (keep what your API expects)
    const last = parseFloat(quote?.price ?? '0')
    const prevClose = parseFloat(quote?.previousClose ?? '0')
    const change = parseFloat(quote?.change ?? '0')
    const changePctStr = String(quote?.changePercent ?? '')
    const changePct = changePctStr.includes('%')
      ? parseFloat(changePctStr.replace('%', ''))
      : parseFloat(changePctStr || '0')

    const duration = Date.now() - startTime

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'market-mcp',
        eventType: 'market_fetch_success',
        message: 'Market data fetched from Alpha Vantage MCP',
        correlationId,
        data: { provider: 'AlphaVantageMCP', symbol: symbol.toUpperCase(), duration_ms: duration },
      })
    )

    return {
      symbol,
      provider: 'AlphaVantageMCP',
      price: last,
      change,
      changePct,
      timestamp: new Date().toISOString(), // Standardize on 'timestamp'
      raw: quote,
    }
  }

  // FALLBACK: existing Stooq logic continues below...

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

    const headers = lines[0].split(',').map(h => h.trim().toLowerCase())
    const latestData = lines[lines.length - 1].split(',').map(v => v.trim())

    const quote: Record<string, any> = {}
    headers.forEach((header, index) => {
      quote[header] = latestData[index]
    })

    // Normalize Stooq to common schema
    const price = parseFloat(quote['close'] || '0')
    const open = parseFloat(quote['open'] || '0')
    // Stooq simple CSV doesn't provide change/prevClose usually, calculating aprox if possible or 0
    const change = 0
    const changePct = 0

    const duration = Date.now() - startTime

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'market-mcp',
        eventType: 'market_fetch_success',
        message: 'Successfully fetched market data',
        correlationId,
        data: { provider: 'Stooq', symbol: normalizedSymbol, price, duration_ms: duration },
      })
    )

    return {
      symbol,
      provider: 'Stooq',
      price,
      change,
      changePct,
      timestamp: new Date().toISOString(),
      raw: quote,
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
