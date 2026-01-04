import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Timeline from './Timeline'
import { TimelineEvent } from '../api/client'

describe('Timeline', () => {
    it('shows empty message when no events', () => {
        render(<Timeline events={[]} />)
        expect(screen.getByText('No timeline events yet')).toBeInTheDocument()
    })

    it('renders timeline events', () => {
        const events: TimelineEvent[] = [
            {
                id: 1,
                eventType: 'market',
                timestamp: '2024-01-01T10:00:00Z',
                payload: { title: 'AAPL stock data' },
            },
            {
                id: 2,
                eventType: 'news',
                timestamp: '2024-01-02T10:00:00Z',
                payload: { headline: 'Tech news' },
            },
        ]

        render(<Timeline events={events} />)

        expect(screen.getByText('market')).toBeInTheDocument()
        expect(screen.getByText('news')).toBeInTheDocument()
        expect(screen.getByText('AAPL stock data')).toBeInTheDocument()
    })

    it('shows JSON payload when no title', () => {
        const events: TimelineEvent[] = [
            {
                id: 1,
                eventType: 'weather',
                timestamp: '2024-01-01T10:00:00Z',
                payload: { temperature: 25 },
            },
        ]

        render(<Timeline events={events} />)

        expect(screen.getByText('weather')).toBeInTheDocument()
        expect(screen.getByText(/temperature/)).toBeInTheDocument()
    })
})
