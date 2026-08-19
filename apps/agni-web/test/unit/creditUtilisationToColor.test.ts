import { describe, it, expect } from 'vitest'
import { creditUilisationToColor } from '~/utils/creditUtilisationToColor'

describe('creditUilisationToColor', () => {
  it('returns green when utilisation is below default ideal (15)', () => {
    expect(creditUilisationToColor(10)).toBe('green')
    expect(creditUilisationToColor(0)).toBe('green')
    expect(creditUilisationToColor(14)).toBe('green')
  })

  it('returns green when utilisation equals default ideal', () => {
    expect(creditUilisationToColor(15)).toBe('green')
  })

  it('returns red when utilisation exceeds default ideal', () => {
    expect(creditUilisationToColor(16)).toBe('red')
    expect(creditUilisationToColor(50)).toBe('red')
    expect(creditUilisationToColor(100)).toBe('red')
  })

  it('returns green when utilisation is below custom ideal', () => {
    expect(creditUilisationToColor(30, 35)).toBe('green')
  })

  it('returns red when utilisation exceeds custom ideal', () => {
    expect(creditUilisationToColor(36, 35)).toBe('red')
  })

  it('returns green when utilisation equals custom ideal', () => {
    expect(creditUilisationToColor(35, 35)).toBe('green')
  })
})
