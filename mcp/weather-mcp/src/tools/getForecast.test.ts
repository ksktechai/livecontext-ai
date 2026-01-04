import { jest, describe, it, expect, beforeEach, afterEach, beforeAll } from '@jest/globals'

// For ESM mocking
jest.unstable_mockModule('node-fetch', () => ({
    default: jest.fn(),
}))

describe('getForecast', () => {
    let getForecast: any
    let mockFetch: any

    beforeAll(async () => {
        const fetchModule = await import('node-fetch')
        mockFetch = fetchModule.default as jest.Mock
        const forecastModule = await import('./getForecast.js')
        getForecast = forecastModule.getForecast
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
        it('should throw error when latitude is missing', async () => {
            await expect(getForecast({ latitude: undefined, longitude: 0 })).rejects.toThrow(
                'latitude and longitude are required'
            )
        })
    })

    describe('Successful forecast fetch', () => {
        it('should fetch and parse weather data', async () => {
            const mockWeatherData = {
                hourly: {
                    time: ['2024-01-01T00:00', '2024-01-01T01:00'],
                    temperature_2m: [20, 21],
                    precipitation: [0, 0.1],
                },
            }

            mockFetch.mockResolvedValue({
                ok: true,
                json: () => Promise.resolve(mockWeatherData),
            })

            const result = await getForecast({ latitude: 40.7128, longitude: -74.006 })

            expect(result.latitude).toBe(40.7128)
            expect(result.provider).toBe('Open-Meteo')
        })
    })

    describe('Error handling', () => {
        it('should throw on API error', async () => {
            mockFetch.mockResolvedValue({
                ok: false,
                statusText: 'Service Unavailable',
            })

            await expect(getForecast({ latitude: 0, longitude: 0 })).rejects.toThrow(
                'Open-Meteo API error'
            )
        })
    })
})
