import type { BalanceBufferLevel } from "~/utils/getBalanceBufferLevel"

export type AccountSummary = {
    totalBalance: number
    disponible: number
    freeze: number
    lock: number
}

export type TotalBalanceBufferIndicator = {
    description: string
    diffBalance: number
    buffer: number
    coverage: number
    isUnderBuffer: boolean
    level: BalanceBufferLevel
}
