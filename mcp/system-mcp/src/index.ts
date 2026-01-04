import express from 'express'
import { alertsCreate } from './tools/alertsCreate.js'
import { dbQueryReadonly } from './tools/dbQueryReadonly.js'

const app = express()
const PORT = process.env.PORT || 8094

app.use(express.json())

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'system-mcp' })
})

app.post('/tools/alerts_create', async (req, res) => {
  try {
    const result = await alertsCreate(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.post('/tools/db_query_readonly', async (req, res) => {
  try {
    const result = await dbQueryReadonly(req.body.parameters, req.body.correlationId)
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
      service: 'system-mcp',
      message: `System MCP server listening on port ${PORT}`,
    })
  )
})
