import express from 'express'
import { ingestRss } from './tools/ingestRss.js'
import { search } from './tools/search.js'
import { analyzeSentiment } from './tools/analyzeSentiment.js'

const app = express()
const PORT = process.env.PORT || 8092

app.use(express.json())

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', service: 'news-mcp' })
})

app.post('/tools/ingest_rss', async (req, res) => {
  try {
    const result = await ingestRss(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.post('/tools/search', async (req, res) => {
  try {
    const result = await search(req.body.parameters, req.body.correlationId)
    res.json(result)
  } catch (error: any) {
    res.status(400).json({ error: error.message })
  }
})

app.post('/tools/analyze_sentiment', async (req, res) => {
  try {
    const result = await analyzeSentiment(req.body.parameters, req.body.correlationId)
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
      service: 'news-mcp',
      message: `News MCP server listening on port ${PORT}`,
    })
  )
})
