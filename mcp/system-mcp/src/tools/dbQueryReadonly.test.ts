import { jest, describe, it, expect, beforeEach, afterEach, beforeAll } from '@jest/globals'

// For ESM mocking
jest.unstable_mockModule('pg', () => {
    const mockQuery = jest.fn()
    const mockEnd = jest.fn()

    return {
        default: {
            Pool: jest.fn(() => ({
                query: mockQuery,
                end: mockEnd,
            })),
        },
    }
})

describe('dbQueryReadonly (SECURITY CRITICAL)', () => {
    let dbQueryReadonly: any
    let mockPool: any

    beforeAll(async () => {
        const pgModule = await import('pg')
        mockPool = new (pgModule.default as any).Pool()
        const dbModule = await import('./dbQueryReadonly.js')
        dbQueryReadonly = dbModule.dbQueryReadonly
    })

    beforeEach(() => {
        jest.clearAllMocks()
        jest.spyOn(console, 'log').mockImplementation(() => { })
        jest.spyOn(console, 'error').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Security - SQL Injection Prevention', () => {
        it('should reject empty query', async () => {
            await expect(dbQueryReadonly({ query: '' })).rejects.toThrow('query is required')
        })

        it('should only allow SELECT queries', async () => {
            await expect(dbQueryReadonly({ query: 'DELETE FROM users' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject DELETE queries', async () => {
            await expect(dbQueryReadonly({ query: 'SELECT * FROM users; DELETE FROM users' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject UPDATE queries', async () => {
            await expect(dbQueryReadonly({ query: 'SELECT * FROM users; UPDATE users SET name = "hacked"' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject INSERT queries', async () => {
            await expect(dbQueryReadonly({ query: 'SELECT * FROM users; INSERT INTO users VALUES (1)' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject DROP queries', async () => {
            await expect(dbQueryReadonly({ query: 'SELECT * FROM users; DROP TABLE users' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject ALTER queries', async () => {
            await expect(dbQueryReadonly({ query: 'SELECT 1; ALTER TABLE users ADD COLUMN hacked VARCHAR' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject CREATE queries', async () => {
            await expect(dbQueryReadonly({ query: 'SELECT 1; CREATE TABLE hacked (id INT)' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })

        it('should reject queries not starting with SELECT', async () => {
            await expect(dbQueryReadonly({ query: 'WITH cte AS (DELETE FROM users) SELECT * FROM cte' })).rejects.toThrow(
                'Only SELECT queries are allowed'
            )
        })
    })

    describe('Valid SELECT queries', () => {
        it('should allow simple SELECT query', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1, name: 'test' }] })

            const result = await dbQueryReadonly({ query: 'SELECT * FROM users' })

            expect(result.rows).toEqual([{ id: 1, name: 'test' }])
            expect(result.rowCount).toBe(1)
        })

        it('should allow SELECT with lowercase', async () => {
            mockPool.query.mockResolvedValue({ rows: [] })

            const result = await dbQueryReadonly({ query: 'select * from users' })

            expect(result.rows).toEqual([])
        })
    })
})
