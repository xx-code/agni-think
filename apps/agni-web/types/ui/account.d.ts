export type EditAccountType = {
    title: string
    type: string
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

export type AccountWithDetailType = AccountType & {
    lockedBalance: number
    freezedBalance: number
}