import pg from 'pg'

const { Pool } = pg

interface DbQueryReadonlyParams {
  query: string
  maxRows?: number
}

export async function dbQueryReadonly(params: DbQueryReadonlyParams, correlationId?: string) {
  const { query, maxRows = 100 } = params

  if (!query) {
    throw new Error('query is required')
  }

  const normalizedQuery = query.trim().toUpperCase()
  if (!normalizedQuery.startsWith('SELECT')) {
    throw new Error('Only SELECT queries are allowed (read-only)')
  }

  if (
    normalizedQuery.includes('DELETE') ||
    normalizedQuery.includes('UPDATE') ||
    normalizedQuery.includes('INSERT') ||
    normalizedQuery.includes('DROP') ||
    normalizedQuery.includes('ALTER') ||
    normalizedQuery.includes('CREATE')
  ) {
    throw new Error('Only SELECT queries are allowed (read-only)')
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'system-mcp',
      eventType: 'db_query_start',
      message: 'Executing read-only database query',
      correlationId,
      data: { queryLength: query.length, maxRows },
    })
  )

  const databaseUrl = process.env.DATABASE_URL || 'postgresql://livecontext:livecontext@localhost:5432/livecontext'
  const pool = new Pool({ connectionString: databaseUrl, max: 5 })

  try {
    const result = await pool.query(`${query} LIMIT ${maxRows}`)

    console.log(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'INFO',
        service: 'system-mcp',
        eventType: 'db_query_success',
        message: 'Database query executed',
        correlationId,
        data: { rowCount: result.rows.length },
      })
    )

    await pool.end()

    return {
      rows: result.rows,
      rowCount: result.rows.length,
      timestamp: new Date().toISOString(),
    }
  } catch (error: any) {
    await pool.end()

    console.error(
      JSON.stringify({
        timestamp: new Date().toISOString(),
        level: 'ERROR',
        service: 'system-mcp',
        eventType: 'db_query_error',
        message: 'Database query failed',
        correlationId,
        data: { error: error.message },
      })
    )

    throw error
  }
}
