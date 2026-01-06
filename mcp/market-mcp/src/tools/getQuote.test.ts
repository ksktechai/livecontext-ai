import { jest, describe, it, expect, beforeEach, afterEach, beforeAll } from '@jest/globals'

// For ESM mocking, we need to use unstable_mockModule
jest.unstable_mockModule('node-fetch', () => ({
    default: jest.fn(),
}))

jest.unstable_mockModule('../alphaVantageMcpClient.js', () => ({
    alphaVantageToolsCall: jest.fn(),
}))

describe('getQuote', () => {
    let getQuote: any
    let mockFetch: any
    let mockAlphaVantageToolsCall: any
    const originalEnv = process.env

    beforeAll(async () => {
        const fetchModule = await import('node-fetch')
        mockFetch = fetchModule.default as jest.Mock

        const avModule = await import('../alphaVantageMcpClient.js')
        mockAlphaVantageToolsCall = avModule.alphaVantageToolsCall as jest.Mock

        const quoteModule = await import('./getQuote.js')
        getQuote = quoteModule.getQuote
    })

    beforeEach(() => {
        jest.clearAllMocks()
        process.env = { ...originalEnv }
        delete process.env.ALPHAVANTAGE_API_KEY // Default to Stooq path

        jest.spyOn(console, 'log').mockImplementation(() => { })
        jest.spyOn(console, 'error').mockImplementation(() => { })
    })

    afterEach(() => {
        process.env = originalEnv
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when symbol is missing', async () => {
            await expect(getQuote({ symbol: '' })).rejects.toThrow('symbol is required')
        })
    })

    describe('Stooq Quote (Default)', () => {
        it('should fetch and parse daily quote data via Stooq', async () => {
            const csvResponse = 'Date,Open,High,Low,Close,Volume\n2024-01-01,150,155,148,152,1000000'

            mockFetch.mockResolvedValue({
                ok: true, // Should be ok: true
                text: () => Promise.resolve(csvResponse),
            })

            const result = await getQuote({ symbol: 'AAPL.US' })

            expect(result.symbol).toBe('AAPL.US')
            expect(result.provider).toBe('Stooq')
            expect(result.price).toBe(152)
            expect(result.raw).toBeDefined()
        })
    })

    describe('Alpha Vantage Quote', () => {
        it('should use Alpha Vantage when API key is present', async () => {
            process.env.ALPHAVANTAGE_API_KEY = 'test-key'

            const avResponse = {
                result: {
                    content: [{
                        type: 'text',
                        text: 'symbol,open,high,low,price,volume,latestDay,previousClose,change,changePercent\nIBM,150,155,148,152.5,1000000,2024-01-01,150,2.5,1.6%'
                    }]
                }
            }

            mockAlphaVantageToolsCall.mockResolvedValue(avResponse)

            const result = await getQuote({ symbol: 'IBM' })

            expect(mockAlphaVantageToolsCall).toHaveBeenCalledWith(
                'GLOBAL_QUOTE',
                { symbol: 'IBM', datatype: 'csv' },
                undefined
            )
            expect(result.provider).toBe('AlphaVantageMCP')
            expect(result.price).toBe(152.5)
            expect(result.change).toBe(2.5)
            expect(result.changePct).toBe(1.6)
            expect(mockFetch).not.toHaveBeenCalled()
        })
    })

    describe('Error handling', () => {
        it('should throw on Stooq API error', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                statusText: 'Internal Server Error',
            })

            await expect(getQuote({ symbol: 'AAPL' })).rejects.toThrow('Stooq API error')
        })

        it('should detect Stooq rate limit exceeded', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                text: () => Promise.resolve('Exceeded the daily hits limit'),
            })

            await expect(getQuote({ symbol: 'AAPL' })).rejects.toThrow('rate limit exceeded')
        })
    })
})
