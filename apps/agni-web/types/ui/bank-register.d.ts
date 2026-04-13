export type BankRegisterType = {
    id: string
    title: string
    active: boolean
    accounts: {
        accountId: string    
        bankAccountId: string
        accountName: string
    }[]
}

