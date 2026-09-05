import type { GetBankRegisterResponse } from "../api/bank-register"

export type BankRegisterType = Omit<GetBankRegisterResponse, 'isActive' | 'bankRegisterId'> & {
    id: string
    isActive: boolean
}