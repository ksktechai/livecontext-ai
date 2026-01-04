import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'
import { search } from './search.js'

describe('search', () => {
    beforeEach(() => {
        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when query is missing', async () => {
            await expect(search({ query: '' })).rejects.toThrow('query is required')
        })

        it('should throw error when query is undefined', async () => {
            await expect(search({ query: undefined as any })).rejects.toThrow('query is required')
        })
    })

    describe('Successful search', () => {
        it('should search with default limit', async () => {
            const result = await search({ query: 'tech news' })

            expect(result.query).toBe('tech news')
            expect(result.results).toBeDefined()
            expect(Array.isArray(result.results)).toBe(true)
            expect(result.timestamp).toBeDefined()
        })

        it('should accept custom limit', async () => {
            const result = await search({ query: 'market', limit: 5 })

            expect(result.query).toBe('market')
            expect(result.results.length).toBeLessThanOrEqual(5)
        })

        it('should include correlationId in logging', async () => {
            await search({ query: 'test' }, 'search-correlation')

            expect(console.log).toHaveBeenCalled()
            const logCall = (console.log as jest.Mock).mock.calls[0][0]
            expect(logCall).toContain('search-correlation')
        })
    })
})
