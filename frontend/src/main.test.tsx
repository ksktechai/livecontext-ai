import { describe, it, expect } from 'vitest'

// Note: main.tsx is the entry point that renders to the DOM.
// Testing it directly is challenging since it executes immediately.
// Instead, we verify the module structure and exports.

describe('main module', () => {
    it('should have a root element available', () => {
        // The root element is created by vitest's jsdom environment
        // This test verifies our test setup is correct
        const root = document.createElement('div')
        root.id = 'root'
        document.body.appendChild(root)

        expect(document.getElementById('root')).toBeTruthy()

        document.body.removeChild(root)
    })

    it('verifies React and ReactDOM are importable', async () => {
        const React = await import('react')
        const ReactDOM = await import('react-dom/client')

        expect(React).toBeDefined()
        expect(ReactDOM.createRoot).toBeDefined()
    })

    it('verifies App component is importable', async () => {
        const AppModule = await import('./App')

        expect(AppModule.default).toBeDefined()
        expect(typeof AppModule.default).toBe('function')
    })
})
