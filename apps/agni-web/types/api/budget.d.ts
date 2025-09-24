export type GetBudgetResponse = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    period: string
    periodTime?: number
    currentBalance: number
    saveGoalIds: string[]
    startDate: Date 
    updateDate: Date
    endDate?: Date
} 

export type GetAllBudgetResponse = {
    id: string,
    title: string,
    target: number,
    realTarget: number,
    saveGoalTarget: number
    period: string
    periodTime?: number
    saveGoalIds: string[]
    currentBalance: number
    startDate: Date
    updateDate: Date
    endDate?: Date
}

export type CreateBudgetRequest = {
    title: string;
    target: number;
    saveGoalIds: string[]
    schedule: {
        period: string;
        periodTime?: number;
        dateStart: string;
        dateEnd?: string
    } 
} 

export type UpdateBudgetRequest = {
    title?: string;
    target?: number;
    saveGoalIds?: string[]
    schedule?: {
        period: string;
        periodTime?: number;
        dateStart: string;
        dateEnd?: string;
    } 
}