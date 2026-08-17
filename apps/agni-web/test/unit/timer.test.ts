import { describe, it, expect, vi, afterEach } from 'vitest'
import { delay } from '~/utils/timer'

describe('delay', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('resolves after the specified time', async () => {
    vi.useFakeTimers()

    let resolved = false
    const promise = delay(1000).then(() => {
      resolved = true
    })

    expect(resolved).toBe(false)

    vi.advanceTimersByTime(1000)
    await promise

    expect(resolved).toBe(true)
  })

  it('does not resolve before the specified time', async () => {
    vi.useFakeTimers()

    let resolved = false
    const promise = delay(1000).then(() => {
      resolved = true
    })

    vi.advanceTimersByTime(500)
    expect(resolved).toBe(false)

    vi.advanceTimersByTime(500)
    await promise

    expect(resolved).toBe(true)
  })
})
