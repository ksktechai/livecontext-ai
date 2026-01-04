import { Logger } from './logger'

describe('Logger', () => {
    let logger: Logger

    beforeEach(() => {
        logger = new Logger('test-service')
        jest.spyOn(console, 'log').mockImplementation(() => { })
        jest.spyOn(console, 'warn').mockImplementation(() => { })
        jest.spyOn(console, 'error').mockImplementation(() => { })
    })

    afterEach(() => {
        jest.restoreAllMocks()
    })

    describe('log', () => {
        it('should log INFO level to console.log', () => {
            logger.log('INFO', 'test_event', 'Test message')

            expect(console.log).toHaveBeenCalled()
            const logArg = (console.log as jest.Mock).mock.calls[0][0]
            expect(logArg).toContain('INFO')
            expect(logArg).toContain('test-service')
            expect(logArg).toContain('test_event')
        })

        it('should log DEBUG level to console.log', () => {
            logger.log('DEBUG', 'debug_event', 'Debug message')

            expect(console.log).toHaveBeenCalled()
        })

        it('should log WARN level to console.warn', () => {
            logger.log('WARN', 'warn_event', 'Warning message')

            expect(console.warn).toHaveBeenCalled()
        })

        it('should log ERROR level to console.error', () => {
            logger.log('ERROR', 'error_event', 'Error message')

            expect(console.error).toHaveBeenCalled()
        })

        it('should default unknown level to console.log', () => {
            logger.log('CUSTOM', 'custom_event', 'Custom message')

            expect(console.log).toHaveBeenCalled()
        })

        it('should include correlationId when provided', () => {
            logger.log('INFO', 'test', 'message', undefined, 'corr-123')

            const logArg = (console.log as jest.Mock).mock.calls[0][0]
            expect(logArg).toContain('corr-123')
        })

        it('should include data when provided', () => {
            logger.log('INFO', 'test', 'message', { key: 'value' })

            const logArg = (console.log as jest.Mock).mock.calls[0][0]
            expect(logArg).toContain('key')
            expect(logArg).toContain('value')
        })
    })

    describe('convenience methods', () => {
        it('info should call log with INFO level', () => {
            logger.info('event', 'message', { data: 'test' }, 'corr-id')

            expect(console.log).toHaveBeenCalled()
            const logArg = (console.log as jest.Mock).mock.calls[0][0]
            expect(logArg).toContain('INFO')
        })

        it('error should call log with ERROR level', () => {
            logger.error('event', 'message')

            expect(console.error).toHaveBeenCalled()
        })

        it('warn should call log with WARN level', () => {
            logger.warn('event', 'message')

            expect(console.warn).toHaveBeenCalled()
        })

        it('debug should call log with DEBUG level', () => {
            logger.debug('event', 'message')

            expect(console.log).toHaveBeenCalled()
            const logArg = (console.log as jest.Mock).mock.calls[0][0]
            expect(logArg).toContain('DEBUG')
        })
    })
})
