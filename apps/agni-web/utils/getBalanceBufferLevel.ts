export type BalanceBufferLevel = 'success' | 'warning' | 'error'

export function getBalanceBufferLevel(buffer: number, totalBalance: number, nearBufferRatio: number = 0.25): BalanceBufferLevel {
    if (totalBalance >= buffer)
        return 'success'

    if (buffer <= 0)
        return 'error'

    const distanceRatio = (buffer - totalBalance) / buffer

    return distanceRatio <= nearBufferRatio ? 'warning' : 'error'
}
