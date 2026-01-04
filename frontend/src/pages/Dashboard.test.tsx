import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import Dashboard from './Dashboard'
import * as api from '../api/client'

// Mock the API module
vi.mock('../api/client', () => ({
    getRecentEvents: vi.fn(),
    subscribeToTimeline: vi.fn(() => vi.fn()), // Returns an unsubscribe function
}))

describe('Dashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('shows loading state initially', () => {
        vi.mocked(api.getRecentEvents).mockReturnValue(new Promise(() => { })) // Never resolves

        render(<Dashboard />)

        expect(screen.getByText('Loading events...')).toBeInTheDocument()
    })

    it('renders timeline after loading events', async () => {
        const mockEvents = [
            { id: 1, eventType: 'market', timestamp: '2024-01-01T10:00:00Z', payload: { title: 'Test' } },
        ]

        vi.mocked(api.getRecentEvents).mockResolvedValue(mockEvents)

        render(<Dashboard />)

        await waitFor(() => {
            expect(screen.queryByText('Loading events...')).not.toBeInTheDocument()
        })

        expect(screen.getByText('Dashboard')).toBeInTheDocument()
        expect(screen.getByText('Live Timeline')).toBeInTheDocument()
    })

    it('handles error when loading events', async () => {
        vi.mocked(api.getRecentEvents).mockRejectedValue(new Error('Network error'))

        render(<Dashboard />)

        await waitFor(() => {
            expect(screen.queryByText('Loading events...')).not.toBeInTheDocument()
        })
    })

    it('subscribes to timeline on mount and unsubscribes on unmount', () => {
        const unsubscribeMock = vi.fn()
        vi.mocked(api.subscribeToTimeline).mockReturnValue(unsubscribeMock)
        vi.mocked(api.getRecentEvents).mockResolvedValue([])

        const { unmount } = render(<Dashboard />)

        expect(api.subscribeToTimeline).toHaveBeenCalledTimes(1)

        unmount()

        expect(unsubscribeMock).toHaveBeenCalledTimes(1)
    })
})
