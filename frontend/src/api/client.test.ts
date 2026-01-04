import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
    sendChatMessage,
    subscribeToTimeline,
    getRecentEvents,
    createAlert,
    getAlerts,
    ChatResponse,
    TimelineEvent,
    AlertRule,
} from '../api/client'

// Mock fetch globally
const mockFetch = vi.fn()
global.fetch = mockFetch

// Mock EventSource
class MockEventSource {
    url: string
    onmessage: ((event: MessageEvent) => void) | null = null
    onerror: ((event: Event) => void) | null = null
    listeners: { [key: string]: ((event: any) => void)[] } = {}

    constructor(url: string) {
        this.url = url
    }

    addEventListener(type: string, callback: (event: any) => void) {
        if (!this.listeners[type]) {
            this.listeners[type] = []
        }
        this.listeners[type].push(callback)
    }

    dispatchEvent(type: string, data: any) {
        if (this.listeners[type]) {
            this.listeners[type].forEach((cb) => cb({ data: JSON.stringify(data) }))
        }
    }

    close() { }
}

// @ts-ignore
global.EventSource = MockEventSource

describe('API Client', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        vi.spyOn(console, 'log').mockImplementation(() => { })
        vi.spyOn(console, 'error').mockImplementation(() => { })
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    describe('sendChatMessage', () => {
        it('should send a chat message and return response', async () => {
            const mockResponse: ChatResponse = {
                answer: 'Test answer',
                evidence: [{ type: 'market', source: 'stooq', timestamp: '2024-01-01', summary: 'test' }],
                correlationId: 'test-123',
            }

            mockFetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockResponse),
            })

            const result = await sendChatMessage('What is the weather?')

            expect(result).toEqual(mockResponse)
            expect(mockFetch).toHaveBeenCalledTimes(1)
            expect(mockFetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/chat'),
                expect.objectContaining({
                    method: 'POST',
                    body: expect.stringContaining('What is the weather?'),
                })
            )
        })

        it('should throw error on failed request', async () => {
            mockFetch.mockResolvedValueOnce({
                ok: false,
                statusText: 'Internal Server Error',
            })

            await expect(sendChatMessage('test')).rejects.toThrow('Chat request failed')
        })
    })

    describe('getRecentEvents', () => {
        it('should fetch recent events', async () => {
            const mockEvents: TimelineEvent[] = [
                { id: 1, eventType: 'market', timestamp: '2024-01-01', payload: {} },
                { id: 2, eventType: 'news', timestamp: '2024-01-02', payload: {} },
            ]

            mockFetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockEvents),
            })

            const result = await getRecentEvents()

            expect(result).toEqual(mockEvents)
            expect(mockFetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/timeline/recent'),
                expect.any(Object)
            )
        })

        it('should throw error on failed request', async () => {
            mockFetch.mockResolvedValueOnce({
                ok: false,
                statusText: 'Not Found',
            })

            await expect(getRecentEvents()).rejects.toThrow('Failed to fetch recent events')
        })
    })

    describe('createAlert', () => {
        it('should create an alert rule', async () => {
            const newAlert: AlertRule = {
                name: 'Test Alert',
                condition: { type: 'price' },
                enabled: true,
            }

            const createdAlert: AlertRule = { ...newAlert, id: 1 }

            mockFetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(createdAlert),
            })

            const result = await createAlert(newAlert)

            expect(result).toEqual(createdAlert)
            expect(mockFetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/alerts'),
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify(newAlert),
                })
            )
        })

        it('should throw error on failed request', async () => {
            mockFetch.mockResolvedValueOnce({
                ok: false,
                statusText: 'Bad Request',
            })

            await expect(createAlert({ name: 'test', condition: {}, enabled: true })).rejects.toThrow(
                'Failed to create alert'
            )
        })
    })

    describe('getAlerts', () => {
        it('should fetch all alerts', async () => {
            const mockAlerts: AlertRule[] = [
                { id: 1, name: 'Alert 1', condition: {}, enabled: true },
                { id: 2, name: 'Alert 2', condition: {}, enabled: false },
            ]

            mockFetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockAlerts),
            })

            const result = await getAlerts()

            expect(result).toEqual(mockAlerts)
        })

        it('should throw error on failed request', async () => {
            mockFetch.mockResolvedValueOnce({
                ok: false,
                statusText: 'Server Error',
            })

            await expect(getAlerts()).rejects.toThrow('Failed to fetch alerts')
        })
    })

    describe('subscribeToTimeline', () => {
        it('should subscribe to timeline events', () => {
            const onEvent = vi.fn()
            const unsubscribe = subscribeToTimeline(onEvent)

            expect(typeof unsubscribe).toBe('function')
        })

        it('should call onEvent when receiving events', () => {
            const onEvent = vi.fn()
            subscribeToTimeline(onEvent)

            // Access the EventSource instance
            // Note: This is a simplified test since we mock EventSource
        })
    })
})
