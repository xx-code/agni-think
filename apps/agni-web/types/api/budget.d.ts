export type GetBudgetResponse = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    savingGoalTarget: number
    savingGoalIds: string[]
    currentBalance: number
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
} 

export type CreateBudgetRequest = {
    title: string;
    target: number;
    savingGoalIds: string[]
    schedule: {
        repeater?: {
            period: string
            interval: number 
        }
        dueDate: string
    } 
} 

export type UpdateBudgetRequest = {
    title?: string;
    target?: number;
    saveGoalIds?: string[]
    schedule?: {
        repeater?: {
            period: string
            interval: number
        }
        dueDate: string
    }  
}