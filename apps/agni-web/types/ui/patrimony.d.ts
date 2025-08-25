export type TypePatrimony = 'Asset' | 'Liability' 

export type EditePatrimony = {
    title: string
    description: string
    categoryId: string
    accountIds: string[]
    type: TypePatrimony
}

export type EditSnapshotPatrimony = {
    balance: number
    date: Calendar
    status: string
}

export type PatrimonyType = {
    id: string
    title: string
    lastSnapshotBalance: number
    currentBalance: number 
    type: TypePatrimony
}

export type PatrimonyDetailType = PatrimonyType & {
    accounts: {
        accountId: string
        title: string
        balance: number
    }[]
} 

export type SnapshotPatrimonyType = {
    balance: number
    date: Date
    status: string
}