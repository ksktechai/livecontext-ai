import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'
import { alertsCreate } from './alertsCreate.js'

describe('alertsCreate', () => {
    beforeEach(() => {
        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when name is missing', async () => {
            await expect(alertsCreate({ name: '', condition: {} })).rejects.toThrow('name is required')
        })

        it('should throw error when condition is missing', async () => {
            await expect(alertsCreate({ name: 'Test Alert', condition: null })).rejects.toThrow('condition is required')
        })

        it('should throw error when condition is undefined', async () => {
            await expect(alertsCreate({ name: 'Test', condition: undefined })).rejects.toThrow('condition is required')
        })
    })

    describe('Successful alert creation', () => {
        it('should create alert with default enabled true', async () => {
            const result = await alertsCreate({
                name: 'Price Alert',
                condition: { type: 'price_above', value: 100 },
            })

            expect(result.alert).toBeDefined()
            expect(result.alert.name).toBe('Price Alert')
            expect(result.alert.enabled).toBe(true)
            expect(result.alert.id).toBeDefined()
            expect(result.alert.createdAt).toBeDefined()
            expect(result.timestamp).toBeDefined()
        })

        it('should create alert with enabled false', async () => {
            const result = await alertsCreate({
                name: 'Disabled Alert',
                condition: { type: 'test' },
                enabled: false,
            })

            expect(result.alert.enabled).toBe(false)
        })

        it('should include correlationId in logging', async () => {
            await alertsCreate({ name: 'Test', condition: {} }, 'create-correlation')

            expect(console.log).toHaveBeenCalled()
            const logCall = (console.log as jest.Mock).mock.calls[0][0]
            expect(logCall).toContain('create-correlation')
        })
    })
})
