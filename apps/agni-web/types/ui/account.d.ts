export type EditAccountType = {
    title: string
    type: string
    creditLimit?: number
    contributionType?: string
    managementType?: string
}

export type AccountType = {
    id: string
    title: string
    type: string
    balance: number
}

export type AccountWithPastBalanceType = AccountType & {
    pastBalanceDetail: {
        balance: number,
        diffPercent: number,
        doIncrease: boolean
    }
}

export type AccountCreditDetailType = {
    creditUtilisation: number
    creditLimit: number
}

export type AccountBrokeDetailType = {
    managementType: string
    type: string
}

export type AccountCheckingDetailType = {
    buffer: number
}

export type AccountWithDetailType = AccountType & {
    lockedBalance: number
    freezedBalance: number
    detail?: AccountCreditDetailType | AccountBrokeDetailType | AccountCheckingDetailType
}