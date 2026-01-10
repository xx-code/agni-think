export type GetBudgetResponse = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    currentBalance: number
    saveGoalIds: string[]
    dueDate: Date
    repeater?: {
        period: string
        interval: number
    }
} 

export type GetAllBudgetResponse = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    saveGoalIds: string[]
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
    saveGoalIds: string[]
    schedule: {
        repeater?: {
            period: string
            interval: numbrer
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
            interval: numbrer
        }
        dueDate: string
    }  
}