import express from 'express'
import { getQuote } from './tools/getQuote.js'
import { computeIndicators } from './tools/computeIndicators.js'
import { detectAnomaly } from './tools/detectAnomaly.js'
import { alphaVantageToolsList } from './alphaVantageMcpClient.js'

const app = express()
const PORT = process.env.PORT || 8091

app.use(express.json())

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'market-mcp' })
})

app.post('/tools/alphavantage_tools_list', async (req, res) => {
  try {
    const correlationId =
        req.body?.correlationId ?? req.header('X-Request-Id') ?? `tools-list-${Date.now()}`
    const result = await alphaVantageToolsList(correlationId)
    res.json(result)
  } catch (err: any) {
    res.status(500).json({ error: err.message ?? String(err) })
  }
})

app.post('/tools/get_quote', async (req, res) => {
  try {
    const result = await getQuote(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.post('/tools/compute_indicators', async (req, res) => {
  try {
    const result = await computeIndicators(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.post('/tools/detect_anomaly', async (req, res) => {
  try {
    const result = await detectAnomaly(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.listen(PORT, () => {
  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'market-mcp',
      message: `Market MCP server listening on port ${PORT}`,
    })
  )
})
