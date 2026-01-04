import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'

// For ESM mocking, we need to use unstable_mockModule
jest.unstable_mockModule('node-fetch', () => ({
    default: jest.fn(),
}))

describe('getQuote', () => {
    let getQuote: any
    let mockFetch: any

    beforeAll(async () => {
        const fetchModule = await import('node-fetch')
        mockFetch = fetchModule.default as jest.Mock
        const quoteModule = await import('./getQuote.js')
        getQuote = quoteModule.getQuote
    })

    beforeEach(() => {
        jest.clearAllMocks()
        jest.spyOn(console, 'log').mockImplementation(() => { })
        jest.spyOn(console, 'error').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when symbol is missing', async () => {
            await expect(getQuote({ symbol: '' })).rejects.toThrow('symbol is required')
        })
    })

    describe('Successful quote fetch', () => {
        it('should fetch and parse daily quote data', async () => {
            const csvResponse = 'Date,Open,High,Low,Close,Volume\n2024-01-01,150,155,148,152,1000000'

            mockFetch.mockResolvedValue({
                ok: true,
                text: () => Promise.resolve(csvResponse),
            })

            const result = await getQuote({ symbol: 'AAPL.US' })

            expect(result.symbol).toBe('AAPL.US')
            expect(result.provider).toBe('Stooq')
            expect(result.quote).toBeDefined()
        })
    })

    describe('Error handling', () => {
        it('should throw on API error', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                statusText: 'Internal Server Error',
            })

            await expect(getQuote({ symbol: 'AAPL' })).rejects.toThrow('Stooq API error')
        })

        it('should detect rate limit exceeded', async () => {
            mockFetch.mockResolvedValue({
                ok: true,
                text: () => Promise.resolve('Exceeded the daily hits limit'),
            })

            await expect(getQuote({ symbol: 'AAPL' })).rejects.toThrow('rate limit exceeded')
        })
    })
})
