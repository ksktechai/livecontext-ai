export interface LogData {
  [key: string]: any
}

export class Logger {
  private service: string

  constructor(service: string) {
    this.service = service
  }

  log(level: string, eventType: string, message: string, data?: LogData, correlationId?: string) {
    const logEntry = {
      timestamp: new Date().toISOString(),
      level,
      service: this.service,
      eventType,
      message,
      ...(correlationId && { correlationId }),
      ...(data && { data }),
    }

    const logMessage = JSON.stringify(logEntry)

    switch (level.toUpperCase()) {
      case 'DEBUG':
      case 'INFO':
        console.log(logMessage)
        break
      case 'WARN':
        console.warn(logMessage)
        break
      case 'ERROR':
        console.error(logMessage)
        break
      default:
        console.log(logMessage)
    }
  }

  info(eventType: string, message: string, data?: LogData, correlationId?: string) {
    this.log('INFO', eventType, message, data, correlationId)
  }

  error(eventType: string, message: string, data?: LogData, correlationId?: string) {
    this.log('ERROR', eventType, message, data, correlationId)
  }

  warn(eventType: string, message: string, data?: LogData, correlationId?: string) {
    this.log('WARN', eventType, message, data, correlationId)
  }

  debug(eventType: string, message: string, data?: LogData, correlationId?: string) {
    this.log('DEBUG', eventType, message, data, correlationId)
  }
}
