import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'
import { detectAnomaly } from './detectAnomaly.js'

describe('detectAnomaly', () => {
    beforeEach(() => {
        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when symbol is missing', async () => {
            await expect(detectAnomaly({ symbol: '' })).rejects.toThrow('symbol is required')
        })

        it('should throw error when symbol is undefined', async () => {
            await expect(detectAnomaly({ symbol: undefined as any })).rejects.toThrow('symbol is required')
        })
    })

    describe('Successful anomaly detection', () => {
        it('should detect anomalies with default threshold', async () => {
            const result = await detectAnomaly({ symbol: 'AAPL' })

            expect(result.symbol).toBe('AAPL')
            expect(result.threshold).toBe(2.0)
            expect(result.anomalies).toBeDefined()
            expect(Array.isArray(result.anomalies)).toBe(true)
            expect(result.timestamp).toBeDefined()
        })

        it('should accept custom threshold', async () => {
            const result = await detectAnomaly({ symbol: 'GOOGL', threshold: 3.0 })

            expect(result.symbol).toBe('GOOGL')
            expect(result.threshold).toBe(3.0)
        })

        it('should include correlationId in logging', async () => {
            await detectAnomaly({ symbol: 'MSFT' }, 'anomaly-correlation')

            expect(console.log).toHaveBeenCalled()
            const logCall = (console.log as jest.Mock).mock.calls[0][0]
            expect(logCall).toContain('anomaly-correlation')
        })
    })
})
