export type CreateProvisionRequest = {
    title: string,
    initialCost: number,
    acquisitionDate: string,
    expectedLifespanMonth: number,
    residualValue: number
}

export type UpdateProvisionRequest = {
    title?: string,
    initialCost?: number,
    acquisitionDate?: string,
    expectedLifespanMonth?: number,
    residualValue?: number
}

export type GetProvisionResponse = {
    id: string,
    title: string,
    initialCost: number,
    acquisitionDate: string,
    expectedLifespanMonth: number,
    residualValue: number
}
