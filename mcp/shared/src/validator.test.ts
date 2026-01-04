import {
    validateRequired,
    validateString,
    validateNumber,
    validateBoolean,
    boundResults,
} from './validator'

describe('validator', () => {
    describe('validateRequired', () => {
        it('should pass for valid values', () => {
            expect(() => validateRequired('test', 'field')).not.toThrow()
            expect(() => validateRequired(123, 'field')).not.toThrow()
            expect(() => validateRequired(false, 'field')).not.toThrow()
            expect(() => validateRequired(0, 'field')).not.toThrow()
        })

        it('should throw for undefined', () => {
            expect(() => validateRequired(undefined, 'field')).toThrow('field is required')
        })

        it('should throw for null', () => {
            expect(() => validateRequired(null, 'field')).toThrow('field is required')
        })

        it('should throw for empty string', () => {
            expect(() => validateRequired('', 'field')).toThrow('field is required')
        })
    })

    describe('validateString', () => {
        it('should pass for valid string', () => {
            expect(() => validateString('test', 'field')).not.toThrow()
        })

        it('should throw for non-string', () => {
            expect(() => validateString(123, 'field')).toThrow('field must be a string')
        })

        it('should throw for string exceeding maxLength', () => {
            expect(() => validateString('toolong', 'field', 5)).toThrow(
                'field must be at most 5 characters'
            )
        })

        it('should pass for string within maxLength', () => {
            expect(() => validateString('test', 'field', 10)).not.toThrow()
        })
    })

    describe('validateNumber', () => {
        it('should pass for valid number', () => {
            expect(() => validateNumber(42, 'field')).not.toThrow()
        })

        it('should throw for non-number', () => {
            expect(() => validateNumber('not a number', 'field')).toThrow('field must be a number')
        })

        it('should throw for NaN', () => {
            expect(() => validateNumber(NaN, 'field')).toThrow('field must be a number')
        })

        it('should throw for number below min', () => {
            expect(() => validateNumber(5, 'field', 10)).toThrow('field must be at least 10')
        })

        it('should throw for number above max', () => {
            expect(() => validateNumber(100, 'field', 0, 50)).toThrow('field must be at most 50')
        })

        it('should pass for number within range', () => {
            expect(() => validateNumber(25, 'field', 10, 50)).not.toThrow()
        })
    })

    describe('validateBoolean', () => {
        it('should pass for true', () => {
            expect(() => validateBoolean(true, 'field')).not.toThrow()
        })

        it('should pass for false', () => {
            expect(() => validateBoolean(false, 'field')).not.toThrow()
        })

        it('should throw for non-boolean', () => {
            expect(() => validateBoolean('true', 'field')).toThrow('field must be a boolean')
            expect(() => validateBoolean(1, 'field')).toThrow('field must be a boolean')
        })
    })

    describe('boundResults', () => {
        it('should limit results to maxResults', () => {
            const input = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
            const result = boundResults(input, 5)
            expect(result).toEqual([1, 2, 3, 4, 5])
        })

        it('should use default maxResults of 100', () => {
            const input = new Array(150).fill(0)
            const result = boundResults(input)
            expect(result.length).toBe(100)
        })

        it('should return all items if less than maxResults', () => {
            const input = [1, 2, 3]
            const result = boundResults(input, 10)
            expect(result).toEqual([1, 2, 3])
        })
    })
})
