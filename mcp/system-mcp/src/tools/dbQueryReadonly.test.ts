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

        it('should allow columns containing keyword substrings (created_at contains CREATE)', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1, created_at: '2026-01-01' }] })

            const result = await dbQueryReadonly({ query: 'SELECT id, created_at FROM events' })

            expect(result.rows).toEqual([{ id: 1, created_at: '2026-01-01' }])
            expect(result.rowCount).toBe(1)
        })

        it('should allow columns like updated_at (contains UPDATE as substring)', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1, updated_at: '2026-01-01' }] })

            const result = await dbQueryReadonly({ query: 'SELECT id, updated_at FROM events' })

            expect(result.rows).toEqual([{ id: 1, updated_at: '2026-01-01' }])
        })

        it('should allow columns like deleted_at (contains DELETE as substring)', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1, deleted_at: null }] })

            const result = await dbQueryReadonly({ query: 'SELECT id, deleted_at FROM events' })

            expect(result.rows).toEqual([{ id: 1, deleted_at: null }])
        })
    })

    describe('LIMIT clause handling', () => {
        it('should not add duplicate LIMIT when query already has LIMIT', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1 }] })

            await dbQueryReadonly({ query: 'SELECT * FROM users LIMIT 10' })

            // Verify query was passed without adding another LIMIT
            expect(mockPool.query).toHaveBeenCalledWith('SELECT * FROM users LIMIT 10')
        })

        it('should append LIMIT when query does not have one', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1 }] })

            await dbQueryReadonly({ query: 'SELECT * FROM users' })

            // Verify LIMIT was appended with default maxRows (100)
            expect(mockPool.query).toHaveBeenCalledWith('SELECT * FROM users LIMIT 100')
        })

        it('should append custom maxRows LIMIT when query does not have one', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1 }] })

            await dbQueryReadonly({ query: 'SELECT * FROM users', maxRows: 50 })

            expect(mockPool.query).toHaveBeenCalledWith('SELECT * FROM users LIMIT 50')
        })

        it('should not add duplicate LIMIT with ORDER BY clause', async () => {
            mockPool.query.mockResolvedValue({ rows: [{ id: 1 }] })

            await dbQueryReadonly({ query: 'SELECT * FROM events ORDER BY created_at DESC LIMIT 10' })

            expect(mockPool.query).toHaveBeenCalledWith('SELECT * FROM events ORDER BY created_at DESC LIMIT 10')
        })
    })
})
