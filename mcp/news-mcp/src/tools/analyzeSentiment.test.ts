import { jest, describe, it, expect, beforeEach, afterEach } from '@jest/globals'
import { analyzeSentiment } from './analyzeSentiment.js'

describe('analyzeSentiment', () => {
    beforeEach(() => {
        jest.spyOn(console, 'log').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('Input validation', () => {
        it('should throw error when text is missing', async () => {
            await expect(analyzeSentiment({ text: '' })).rejects.toThrow('text is required')
        })

        it('should throw error when text is undefined', async () => {
            await expect(analyzeSentiment({ text: undefined as any })).rejects.toThrow('text is required')
        })
    })

    describe('Successful sentiment analysis', () => {
        it('should analyze sentiment for given text', async () => {
            const result = await analyzeSentiment({ text: 'This is great news!' })

            expect(result.sentiment).toBeDefined()
            expect(result.sentiment.score).toBeDefined()
            expect(result.sentiment.magnitude).toBeDefined()
            expect(result.sentiment.label).toBeDefined()
            expect(result.timestamp).toBeDefined()
        })

        it('should include correlationId in logging', async () => {
            await analyzeSentiment({ text: 'Test text' }, 'sentiment-correlation')

            expect(console.log).toHaveBeenCalled()
            const logCall = (console.log as jest.Mock).mock.calls[0][0]
            expect(logCall).toContain('sentiment-correlation')
        })
    })
})
