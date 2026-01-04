import { jest, describe, it, expect, beforeEach, afterEach, beforeAll } from '@jest/globals'

// For ESM mocking, we need to use unstable_mockModule
jest.unstable_mockModule('rss-parser', () => {
    const mockParseURL = jest.fn()
    return {
        default: jest.fn().mockImplementation(() => ({
            parseURL: mockParseURL,
        })),
    }
})

describe('ingestRss', () => {
    let ingestRss: any
    let Parser: any
    let mockParseURL: any

    beforeAll(async () => {
        Parser = (await import('rss-parser')).default
        mockParseURL = new Parser().parseURL
        const rssModule = await import('./ingestRss.js')
        ingestRss = rssModule.ingestRss
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
        it('should throw error when feedUrl is missing', async () => {
            await expect(ingestRss({ feedUrl: '' })).rejects.toThrow('feedUrl is required')
        })
    })

    describe('Successful RSS ingestion', () => {
        it('should parse RSS feed and return items', async () => {
            mockParseURL.mockResolvedValue({
                title: 'Tech News',
                items: [
                    {
                        title: 'Article 1',
                        link: 'https://example.com/1',
                        pubDate: '2024-01-01T10:00:00Z',
                        contentSnippet: 'Summary of article 1',
                    },
                ],
            })

            const result = await ingestRss({ feedUrl: 'https://example.com/rss' })

            expect(result.feedTitle).toBe('Tech News')
            expect(result.items.length).toBe(1)
        })
    })

    describe('Error handling', () => {
        it('should handle parser errors', async () => {
            mockParseURL.mockRejectedValue(new Error('Network error'))

            await expect(ingestRss({ feedUrl: 'https://invalid.com/rss' })).rejects.toThrow('Network error')
        })
    })
})
