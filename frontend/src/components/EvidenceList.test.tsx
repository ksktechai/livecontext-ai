import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import EvidenceList from './EvidenceList'
import { Evidence } from '../api/client'

describe('EvidenceList', () => {
    it('shows empty message when no evidence', () => {
        render(<EvidenceList evidence={[]} />)
        expect(screen.getByText('No evidence available')).toBeInTheDocument()
    })

    it('renders evidence items', () => {
        const evidence: Evidence[] = [
            {
                type: 'market',
                source: 'stooq',
                timestamp: '2024-01-01T10:00:00Z',
                summary: 'AAPL price is $150',
            },
            {
                type: 'weather',
                source: 'open-meteo',
                timestamp: '2024-01-02T10:00:00Z',
                summary: 'Temperature is 25°C',
            },
        ]

        render(<EvidenceList evidence={evidence} />)

        expect(screen.getByText('market')).toBeInTheDocument()
        expect(screen.getByText('weather')).toBeInTheDocument()
        expect(screen.getByText('AAPL price is $150')).toBeInTheDocument()
        expect(screen.getByText('Temperature is 25°C')).toBeInTheDocument()
        expect(screen.getByText(/stooq/)).toBeInTheDocument()
        expect(screen.getByText(/open-meteo/)).toBeInTheDocument()
    })

    it('displays source information', () => {
        const evidence: Evidence[] = [
            {
                type: 'news',
                source: 'reddit',
                timestamp: '2024-01-01T10:00:00Z',
                summary: 'Latest tech news',
            },
        ]

        render(<EvidenceList evidence={evidence} />)

        expect(screen.getByText(/reddit/)).toBeInTheDocument()
        expect(screen.getByText('Latest tech news')).toBeInTheDocument()
    })
})
