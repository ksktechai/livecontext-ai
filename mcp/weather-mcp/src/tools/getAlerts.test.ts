import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'
import { getAlerts } from './getAlerts.js'

describe('getAlerts', () => {
    beforeEach(() => {
        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when latitude is missing', async () => {
            await expect(getAlerts({ latitude: undefined as any, longitude: 0 })).rejects.toThrow(
                'latitude and longitude are required'
            )
        })

        it('should throw error when longitude is missing', async () => {
            await expect(getAlerts({ latitude: 0, longitude: undefined as any })).rejects.toThrow(
                'latitude and longitude are required'
            )
        })
    })

    describe('Successful alert fetch', () => {
        it('should fetch weather alerts', async () => {
            const result = await getAlerts({ latitude: 40.7128, longitude: -74.006 })

            expect(result.latitude).toBe(40.7128)
            expect(result.longitude).toBe(-74.006)
            expect(result.alerts).toBeDefined()
            expect(Array.isArray(result.alerts)).toBe(true)
            expect(result.timestamp).toBeDefined()
        })

        it('should include correlationId in logging', async () => {
            await getAlerts({ latitude: 0, longitude: 0 }, 'alerts-correlation')

            expect(console.log).toHaveBeenCalled()
            const logCall = (console.log as jest.Mock).mock.calls[0][0]
            expect(logCall).toContain('alerts-correlation')
        })
    })
})
