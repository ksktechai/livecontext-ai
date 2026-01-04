export function validateRequired(value: any, fieldName: string): void {
  if (value === undefined || value === null || value === '') {
    throw new Error(`${fieldName} is required`)
  }
}

export function validateString(value: any, fieldName: string, maxLength?: number): void {
  validateRequired(value, fieldName)
  if (typeof value !== 'string') {
    throw new Error(`${fieldName} must be a string`)
  }
  if (maxLength && value.length > maxLength) {
    throw new Error(`${fieldName} must be at most ${maxLength} characters`)
  }
}

export function validateNumber(value: any, fieldName: string, min?: number, max?: number): void {
  validateRequired(value, fieldName)
  if (typeof value !== 'number' || isNaN(value)) {
    throw new Error(`${fieldName} must be a number`)
  }
  if (min !== undefined && value < min) {
    throw new Error(`${fieldName} must be at least ${min}`)
  }
  if (max !== undefined && value > max) {
    throw new Error(`${fieldName} must be at most ${max}`)
  }
}

export function validateBoolean(value: any, fieldName: string): void {
  if (typeof value !== 'boolean') {
    throw new Error(`${fieldName} must be a boolean`)
  }
}

export function boundResults<T>(results: T[], maxResults: number = 100): T[] {
  return results.slice(0, maxResults)
}
