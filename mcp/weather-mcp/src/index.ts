import express from 'express'
import { getForecast } from './tools/getForecast.js'
import { getAlerts } from './tools/getAlerts.js'

const app = express()
const PORT = process.env.PORT || 8093

app.use(express.json())

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'weather-mcp' })
})

app.post('/tools/get_forecast', async (req, res) => {
  try {
    const result = await getForecast(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.post('/tools/get_alerts', async (req, res) => {
  try {
    const result = await getAlerts(req.body.parameters, req.body.correlationId)
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
      service: 'weather-mcp',
      message: `Weather MCP server listening on port ${PORT}`,
    })
  )
})
