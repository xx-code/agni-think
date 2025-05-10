export type ItemSaveGoal = {
    itemId: string
    title: string
    url: string
}

export type SaveGoal = {
    saveGoalId: string
    name: string
    description: string
    target: number 
    balance: number
    items: ItemSaveGoal[]
}