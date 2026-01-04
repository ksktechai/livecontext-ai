import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'
import { computeIndicators } from './computeIndicators.js'

describe('computeIndicators', () => {
    beforeEach(() => {
        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when symbol is missing', async () => {
            await expect(computeIndicators({ symbol: '' })).rejects.toThrow('symbol is required')
        })

        it('should throw error when symbol is undefined', async () => {
            await expect(computeIndicators({ symbol: undefined as any })).rejects.toThrow('symbol is required')
        })
    })

    describe('Successful indicator computation', () => {
        it('should compute indicators with default indicators', async () => {
            const result = await computeIndicators({ symbol: 'AAPL' })

            expect(result.symbol).toBe('AAPL')
            expect(result.indicators).toBeDefined()
            expect(result.indicators.sma_20).toBeDefined()
            expect(result.indicators.rsi_14).toBeDefined()
            expect(result.timestamp).toBeDefined()
        })

        it('should accept custom indicators list', async () => {
            const result = await computeIndicators({ symbol: 'GOOGL', indicators: ['macd'] })

            expect(result.symbol).toBe('GOOGL')
            expect(result.indicators).toBeDefined()
        })

        it('should include correlationId in logging', async () => {
            await computeIndicators({ symbol: 'MSFT' }, 'test-correlation')

            expect(console.log).toHaveBeenCalled()
            const logCall = (console.log as jest.Mock).mock.calls[0][0]
            expect(logCall).toContain('test-correlation')
        })
    })
})
