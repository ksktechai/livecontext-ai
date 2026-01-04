import Parser from 'rss-parser'
import crypto from 'crypto'

interface IngestRssParams {
  feedUrl: string
}

export async function ingestRss(params: IngestRssParams, correlationId?: string) {
  const startTime = Date.now()
  const { feedUrl } = params

  if (!feedUrl) {
    throw new Error('feedUrl is required')
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'news-mcp',
      eventType: 'rss_ingest_start',
      message: 'Ingesting RSS feed',
      correlationId,
      data: { feedUrl },
    })
  )

  try {
    const parser = new Parser()
    const feed = await parser.parseURL(feedUrl)

    const items = feed.items.slice(0, 10).map((item) => ({
      id: crypto.createHash('md5').update(item.link || item.guid || '').digest('hex'),
      title: item.title || '',
      link: item.link || '',
      publishedAt: item.pubDate ? new Date(item.pubDate).toISOString() : new Date().toISOString(),
      source: feed.title || 'Unknown',
      summary: item.contentSnippet || item.content?.substring(0, 200) || '',
    }))

    const duration = Date.now() - startTime

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'news-mcp',
        eventType: 'rss_ingest_success',
        message: 'Successfully ingested RSS feed',
        correlationId,
        data: { feedUrl, itemCount: items.length, duration_ms: duration },
      })
    )

    return {
      items,
      feedTitle: feed.title || 'Unknown',
      feedUrl,
      timestamp: new Date().toISOString(),
    }
  } catch (error: any) {
    const duration = Date.now() - startTime

    console.error(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'ERROR',
        service: 'news-mcp',
        eventType: 'rss_ingest_error',
        message: 'Failed to ingest RSS feed',
        correlationId,
        data: { feedUrl, error: error.message, duration_ms: duration },
      })
    )

    throw error
  }
}
