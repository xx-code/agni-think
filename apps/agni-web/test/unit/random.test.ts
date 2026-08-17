import { describe, it, expect } from 'vitest'
import { generateUUID } from '~/utils/random'

describe('generateUUID', () => {
  it('returns a string', () => {
    expect(typeof generateUUID()).toBe('string')
  })

  it('returns a valid UUID v4 format', () => {
    const uuid = generateUUID()
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
    expect(uuid).toMatch(uuidRegex)
  })

  it('returns unique values on multiple calls', () => {
    const uuids = new Set(Array.from({ length: 50 }, () => generateUUID()))
    expect(uuids.size).toBe(50)
  })
})
