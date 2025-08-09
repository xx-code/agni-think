export type GetBudgetResponse = {
    id: string,
    title: string,
    target: number,
    period: string
    periodTime?: number
    currentBalance: number
    startDate: string
    updateDate: string
    endDate?: string
} 

export type GetAllBudgetResponse = {
    id: string,
    title: string,
    target: number,
    period: string
    periodTime?: number
    currentBalance: number
    startDate: string
    updateDate: string
    endDate?: string
}

export type CreateBudgetRequest = {
    title: string;
    target: number;
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
    schedule?: {
        period: string;
        periodTime?: number;
        dateStart: string;
        dateEnd?: string;
    } 
}