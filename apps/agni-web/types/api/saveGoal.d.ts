export type GetSaveGoalResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number
    desirValue: number
    importance: number
    wishDueDate?: string
    items: {
        title: string
        link: string
        price: number
        htmlToTrack: string
    }[] 
}

export type GetAllSaveGoalResponse = {
    id: string,
    title: string,
    description: string,
    target: number,
    balance: number,
    desirValue: number
    importance: number
    wishDueDate?: string
    items: {
        title: string
        price: number
        link: string
        htmlToTrack: string
    }[]
}

export type CreateSaveGoalRequest = {
    target: number;
    title: string;
    description: string
    desirValue: number
    importance: number
    wishDueDate?: string
    items: {
        title: string
        price: number
        link: string
        htmlToTrack: string
    }[]
}

export type UpdateSaveGoalRequest = {
    target?: number
    title?: string
    description?: string
    desirValue?: number
    importance?: number
    wishDueDate?: string
    items?: {
        id: string,
        title: string,
        link: string,
        htmlToTrack: string
        price: number
    }[]
}

export type IncreaseSaveGoalRequest = {
    id: string
    accountId: string
    increaseAmount: number
}

export type DecreaseSaveGoalRequest = {
    id: string
    accountId: string
    decreaseAmount: number
}

export type DeleteSaveGoalRequest = {
    accountDepositId: string
}
