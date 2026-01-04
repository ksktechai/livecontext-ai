import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Alerts from './Alerts'
import * as api from '../api/client'

// Mock the API module
vi.mock('../api/client', () => ({
    getAlerts: vi.fn(),
    createAlert: vi.fn(),
}))

describe('Alerts', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders alerts page', async () => {
        vi.mocked(api.getAlerts).mockResolvedValue([])

        render(<Alerts />)

        expect(screen.getByText('Alerts')).toBeInTheDocument()
        expect(screen.getByText('Create New Alert')).toBeInTheDocument()

        await waitFor(() => {
            expect(screen.getByText('No alerts configured yet')).toBeInTheDocument()
        })
    })

    it('loads and displays existing alerts', async () => {
        const mockAlerts = [
            { id: 1, name: 'AAPL Price Alert', condition: {}, enabled: true },
            { id: 2, name: 'News Alert', condition: {}, enabled: false },
        ]

        vi.mocked(api.getAlerts).mockResolvedValue(mockAlerts)

        render(<Alerts />)

        await waitFor(() => {
            expect(screen.getByText('AAPL Price Alert')).toBeInTheDocument()
            expect(screen.getByText('News Alert')).toBeInTheDocument()
        })

        expect(screen.getByText('Enabled')).toBeInTheDocument()
        expect(screen.getByText('Disabled')).toBeInTheDocument()
    })

    it('creates new alert when form is submitted', async () => {
        vi.mocked(api.getAlerts).mockResolvedValue([])
        vi.mocked(api.createAlert).mockResolvedValue({ id: 1, name: 'New Alert', condition: {}, enabled: true })

        render(<Alerts />)

        const input = screen.getByPlaceholderText(/Alert name/i)
        const button = screen.getByRole('button', { name: /Create Alert/i })

        await userEvent.type(input, 'New Alert')
        await userEvent.click(button)

        await waitFor(() => {
            expect(api.createAlert).toHaveBeenCalledWith(
                expect.objectContaining({ name: 'New Alert', enabled: true })
            )
        })

        // Should reload alerts after creation
        expect(api.getAlerts).toHaveBeenCalledTimes(2)
    })

    it('does not submit empty alert name', async () => {
        vi.mocked(api.getAlerts).mockResolvedValue([])

        render(<Alerts />)

        const button = screen.getByRole('button', { name: /Create Alert/i })
        await userEvent.click(button)

        expect(api.createAlert).not.toHaveBeenCalled()
    })

    it('handles error when creating alert', async () => {
        vi.mocked(api.getAlerts).mockResolvedValue([])
        vi.mocked(api.createAlert).mockRejectedValue(new Error('Failed'))

        render(<Alerts />)

        const input = screen.getByPlaceholderText(/Alert name/i)
        const button = screen.getByRole('button', { name: /Create Alert/i })

        await userEvent.type(input, 'Test')
        await userEvent.click(button)

        // Should not crash, just log error
        await waitFor(() => {
            expect(api.createAlert).toHaveBeenCalled()
        })
    })
})
