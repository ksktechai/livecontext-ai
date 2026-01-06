import { jest, describe, it, expect, beforeEach, afterEach, beforeAll } from '@jest/globals'

jest.unstable_mockModule('node-fetch', () => ({
    default: jest.fn(),
}))

describe('alphaVantageMcpClient', () => {
    let alphaVantageToolsCall: any
    let mockFetch: any
    const originalEnv = process.env

    beforeAll(async () => {
        const fetchModule = await import('node-fetch')
        mockFetch = fetchModule.default as jest.Mock
        // Import module under test after mocking
        const clientModule = await import('./alphaVantageMcpClient.js')
        alphaVantageToolsCall = clientModule.alphaVantageToolsCall
    })

    beforeEach(() => {
        jest.clearAllMocks()
        process.env = { ...originalEnv }
        process.env.ALPHAVANTAGE_API_KEY = 'test-key'
        // Reset initialization state if possible? 
        // The module has top-level state `initialized` which is hard to reset without reloading module.
        // For now, we assume ensureInitialized handles repeated calls safely.

        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        process.env = originalEnv
        jest.restoreAllMocks()
    })

    it('should initialize and call tool', async () => {
        // Mock responses for:
        // 1. initialize
        // 2. notifications/initialized
        // 3. tools/call
        mockFetch
            .mockResolvedValueOnce({
                ok: true,
                text: () => Promise.resolve(JSON.stringify({ jsonrpc: '2.0', id: 1, result: { capabilities: {} } })),
                headers: { get: () => 'sess-1' }
            })
            .mockResolvedValueOnce({
                ok: true,
                text: () => Promise.resolve(JSON.stringify({ jsonrpc: '2.0', method: 'notifications/initialized' })),
                headers: { get: () => 'sess-1' }
            })
            .mockResolvedValueOnce({
                ok: true,
                text: () => Promise.resolve(JSON.stringify({ jsonrpc: '2.0', id: 2, result: { content: [{ type: 'text', text: 'result' }] } })),
                headers: { get: () => 'sess-1' }
            })

        const result = await alphaVantageToolsCall('GLOBAL_QUOTE', { symbol: 'IBM' }, 'corr-1')

        expect(result).toEqual({ jsonrpc: '2.0', id: 2, result: { content: [{ type: 'text', text: 'result' }] } })
        expect(mockFetch).toHaveBeenCalledTimes(3) // init, notified, call (if first time) OR just call if already initialized. 
        // CHECK: Since module state persists across tests in same file, earlier tests might initialize it.
        // For robustness, subsequent calls might only trigger 1 fetch.
    })

    it('should throw if API key is missing', async () => {
        delete process.env.ALPHAVANTAGE_API_KEY
        // Depending on implementation, checking key happens inside endpointUrl() called by postJsonRpc
        // We might need to reset initialization or mock fetch to fail?
        // Actually, endpointUrl() throws locally.

        // However, if already initialized...
        // The `endpointUrl` is called inside `postJsonRpc`.

        // We need to handle the "already initialized" issue. Module-level variables are tricky.
        // But `endpointUrl` is called every request.

        // Because of the module-level `initialized` flag, if the previous test ran, `initialized` is true.
        // So `ensureInitialized` returns early.
        // `alphaVantageToolsCall` then calls `postJsonRpc`.
        // `postJsonRpc` calls `endpointUrl`.
        // `endpointUrl` checks `apiKey()`.

        await expect(alphaVantageToolsCall('TEST', {})).rejects.toThrow('ALPHAVANTAGE_API_KEY is not set')
    })
})
