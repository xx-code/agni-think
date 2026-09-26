import type { ListResponse } from "~/types/api";
import type { GetAllSpendingPeriodResponse, GetSpendingPeriodResponse } from "~/types/api/spending-period";
import type { AllSpendingPeriod, SpendingPeriod } from "~/types/ui/spending-period";

export function spendingPeriodResponseToSpendingPeriod(data: GetSpendingPeriodResponse): SpendingPeriod {
    return {
        ...data,
        startDate: new Date(data.startDate), 
        endDate: new Date(data.endDate), 
        snapshot: data.snapshot,
        forcast: {
            ...data.forcast,  
            incomeItems: data.forcast.incomeItems.map(i => ({...i, isAchieved: i.achieved})),
            fixExpenseItems: data.forcast.fixExpenseItems.map(i => ({...i, isAchieved: i.achieved})),
            variableExpenseItems: data.forcast.variableExpenseItems.map(i => ({...i, isAchieved: i.achieved})),
            achievedWishedItems: data.forcast.achievedWishedItems.map(i => ({...i, isAchieved: i.achieved}))
        }
    }
} 

function getAllSpendingPeriodResponseToAllSpendingPeriod(data: GetAllSpendingPeriodResponse): AllSpendingPeriod {
    return {
        ...data,
        startDate: new Date(data.startDate), 
        endDate: new Date(data.endDate), 
        snapshot: data.snapshot
    }
}

export function listSpendingPeriodResponseToSpendingPeriod(data: ListResponse<GetAllSpendingPeriodResponse>): ListResponse<AllSpendingPeriod> {
    return {
        items: data.items.map(i => getAllSpendingPeriodResponseToAllSpendingPeriod(i)),
        total: data.total
    }
}