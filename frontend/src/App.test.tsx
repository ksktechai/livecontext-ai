import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import App from './App'
import * as api from './api/client'

// Mock the API module to avoid actual network calls
vi.mock('./api/client', () => ({
    getRecentEvents: vi.fn(() => Promise.resolve([])),
    subscribeToTimeline: vi.fn(() => vi.fn()),
    getAlerts: vi.fn(() => Promise.resolve([])),
    sendChatMessage: vi.fn(),
    createAlert: vi.fn(),
}))

describe('App', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('renders the app with navigation', () => {
        render(<App />)

        expect(screen.getByText('LiveContext-AI')).toBeInTheDocument()
        expect(screen.getByRole('link', { name: /Dashboard/i })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: /Chat/i })).toBeInTheDocument()
        expect(screen.getByRole('link', { name: /Alerts/i })).toBeInTheDocument()
    })

    it('renders Dashboard page on default route', () => {
        render(<App />)

        // Use getAllByText since "Dashboard" appears in both nav link and page title
        const dashboardElements = screen.getAllByText('Dashboard')
        expect(dashboardElements.length).toBeGreaterThanOrEqual(1)
    })

    it('has correct navigation links', () => {
        render(<App />)

        const dashboardLink = screen.getByRole('link', { name: /Dashboard/i })
        const chatLink = screen.getByRole('link', { name: /Chat/i })
        const alertsLink = screen.getByRole('link', { name: /Alerts/i })

        expect(dashboardLink).toHaveAttribute('href', '/')
        expect(chatLink).toHaveAttribute('href', '/chat')
        expect(alertsLink).toHaveAttribute('href', '/alerts')
    })

    it('renders the main content area', () => {
        render(<App />)

        expect(screen.getByRole('main')).toBeInTheDocument()
    })

    it('displays the brand name', () => {
        render(<App />)

        expect(screen.getByText('LiveContext-AI')).toBeInTheDocument()
    })
})
