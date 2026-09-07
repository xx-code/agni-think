import type { GetBankRegisterResponse } from "../api/bank-register"

export type EditBankLinker = {
    title: string
    accessCode: string
    bankAccounts: {id: string, name: string }[]
}

export type EditAddBankRegister = {
    bankRegisterId: string
    accountId: string
    bankRegisterAccountId: string
}

type BankRegisterAccount = {
    accountId?: string    
    bankAccountId: string
    accountName: string
    bankName: string
    isActive: boolean
}

export type BankRegisterType = Omit<GetBankRegisterResponse, 'isActive' | 'bankRegisterId' | 'accounts'> & {
    id: string
    isActive: boolean
    accounts: BankRegisterAccount[]
}