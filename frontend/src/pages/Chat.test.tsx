import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Chat from './Chat'
import * as api from '../api/client'

// Mock the API module
vi.mock('../api/client', () => ({
    sendChatMessage: vi.fn(),
}))

describe('Chat', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders chat page', () => {
        render(<Chat />)

        expect(screen.getByText('Chat')).toBeInTheDocument()
        expect(screen.getByPlaceholderText(/Ask a question/i)).toBeInTheDocument()
        expect(screen.getByRole('button', { name: /Send Question/i })).toBeInTheDocument()
    })

    it('sends message when form is submitted', async () => {
        const mockResponse = {
            answer: 'The weather is sunny',
            evidence: [],
            correlationId: 'test-123',
        }

        vi.mocked(api.sendChatMessage).mockResolvedValue(mockResponse)

        render(<Chat />)

        const textarea = screen.getByPlaceholderText(/Ask a question/i)
        const button = screen.getByRole('button', { name: /Send Question/i })

        await userEvent.type(textarea, 'What is the weather?')
        await userEvent.click(button)

        await waitFor(() => {
            expect(api.sendChatMessage).toHaveBeenCalledWith('What is the weather?')
        })

        await waitFor(() => {
            expect(screen.getByText('The weather is sunny')).toBeInTheDocument()
        })
    })

    it('shows error when request fails', async () => {
        vi.mocked(api.sendChatMessage).mockRejectedValue(new Error('Network error'))

        render(<Chat />)

        const textarea = screen.getByPlaceholderText(/Ask a question/i)
        const button = screen.getByRole('button', { name: /Send Question/i })

        await userEvent.type(textarea, 'Test question')
        await userEvent.click(button)

        await waitFor(() => {
            expect(screen.getByText(/Error: Network error/i)).toBeInTheDocument()
        })
    })

    it('shows loading state while processing', async () => {
        vi.mocked(api.sendChatMessage).mockImplementation(
            () => new Promise((resolve) => setTimeout(() => resolve({
                answer: 'Test',
                evidence: [],
                correlationId: 'test',
            }), 100))
        )

        render(<Chat />)

        const textarea = screen.getByPlaceholderText(/Ask a question/i)
        const button = screen.getByRole('button', { name: /Send Question/i })

        await userEvent.type(textarea, 'Test')
        await userEvent.click(button)

        expect(screen.getByText('Processing...')).toBeInTheDocument()
    })

    it('does not submit empty question', async () => {
        render(<Chat />)

        const button = screen.getByRole('button', { name: /Send Question/i })
        await userEvent.click(button)

        expect(api.sendChatMessage).not.toHaveBeenCalled()
    })

    it('displays evidence when available', async () => {
        const mockResponse = {
            answer: 'Test answer',
            evidence: [
                { type: 'market', source: 'stooq', timestamp: '2024-01-01T10:00:00Z', summary: 'AAPL data' },
            ],
            correlationId: 'test-123',
        }

        vi.mocked(api.sendChatMessage).mockResolvedValue(mockResponse)

        render(<Chat />)

        const textarea = screen.getByPlaceholderText(/Ask a question/i)
        const button = screen.getByRole('button', { name: /Send Question/i })

        await userEvent.type(textarea, 'Stock price?')
        await userEvent.click(button)

        await waitFor(() => {
            expect(screen.getByText('Evidence')).toBeInTheDocument()
        })
    })
})
