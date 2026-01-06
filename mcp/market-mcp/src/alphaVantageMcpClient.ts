import fetch from 'node-fetch'

type JsonRpc = Record<string, any>

let sessionId: string | undefined
let initialized = false

const PROTOCOL_VERSION = '2025-03-26' // MCP protocol version used in many examples; server tolerates this.

function baseUrl(): string {
    return process.env.ALPHAVANTAGE_MCP_URL || 'https://mcp.alphavantage.co/mcp'
}

function apiKey(): string | undefined {
    return process.env.ALPHAVANTAGE_API_KEY
}

function endpointUrl(): string {
    const key = apiKey()
    if (!key) throw new Error('ALPHAVANTAGE_API_KEY is not set')
    // Official connection format supports apikey in query string
    return `${baseUrl()}?apikey=${encodeURIComponent(key)}`
}

function safeUrlForLogs(): string {
    return `${baseUrl()}?apikey=REDACTED`
}

async function postJsonRpc(body: JsonRpc, correlationId?: string): Promise<any> {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        Accept: 'application/json, text/event-stream',
    }
    if (sessionId) headers['Mcp-Session-Id'] = sessionId

    console.log(
        JSON.stringify({
            timestamp: new Date().toISOString(),
            level: 'DEBUG',
            service: 'market-mcp',
            eventType: 'alphavantage_mcp_request',
            message: 'Sending JSON-RPC to Alpha Vantage MCP',
            correlationId,
            data: { url: safeUrlForLogs(), method: body.method, id: body.id ?? null },
        })
    )

    const res = await fetch(endpointUrl(), { method: 'POST', headers, body: JSON.stringify(body) })

    // Capture session header if present (MCP streamable-http session mgmt)
    const sid = res.headers.get('Mcp-Session-Id')
    if (sid) sessionId = sid

    const text = await res.text()
    let json: any
    try {
        json = text ? JSON.parse(text) : {}
    } catch {
        json = { raw: text }
    }

    if (!res.ok) {
        throw new Error(`AlphaVantage MCP HTTP ${res.status}: ${JSON.stringify(json).slice(0, 500)}`)
    }

    return json
}

async function ensureInitialized(correlationId?: string) {
    if (initialized) return

    // initialize
    await postJsonRpc(
        {
            jsonrpc: '2.0',
            id: 1,
            method: 'initialize',
            params: {
                protocolVersion: PROTOCOL_VERSION,
                capabilities: { roots: {}, sampling: {} },
                clientInfo: { name: 'livecontext-ai-market-mcp', version: '0.1.0' },
            },
        },
        correlationId
    )

    // notifications/initialized (no id)
    await postJsonRpc(
        {
            jsonrpc: '2.0',
            method: 'notifications/initialized',
        },
        correlationId
    )

    initialized = true
}

export async function alphaVantageToolsList(correlationId?: string) {
    await ensureInitialized(correlationId)

    const result = await postJsonRpc(
        {
            jsonrpc: '2.0',
            id: 100,
            method: 'tools/list',
            params: {},
        },
        correlationId
    )

    return result
}

export async function alphaVantageToolsCall(
    toolName: string,
    args: Record<string, any>,
    correlationId?: string
) {
    await ensureInitialized(correlationId)

    const result = await postJsonRpc(
        {
            jsonrpc: '2.0',
            id: 2,
            method: 'tools/call',
            params: { name: toolName, arguments: args },
        },
        correlationId
    )

    return result
}