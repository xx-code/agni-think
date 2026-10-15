import type { ListResponse } from "~/types/api";
import type { GetAllSpendingPeriodResponse, GetSpendingPeriodResponse } from "~/types/api/spending-period";
import type { SpendingPeriodStateType } from "~/types/constants/spendingPeriod";
import type { List } from "~/types/ui";
import type { AllSpendingPeriod, SpendingPeriod } from "~/types/ui/spending-period";

export function toSpendingPeriod(data: GetSpendingPeriodResponse): SpendingPeriod {
    return {
        ...data,
        startDate: new Date(data.startDate),
        endDate: new Date(data.endDate),
        state: data.state as SpendingPeriodStateType,
        forcast: data.forcast ? {
            ...data.forcast, 
            incomeItems: data.forcast.incomeItems.map(i => ({...i, isArchieved: i.achieved, isClosed: i.closed})),
            fixExpenseItems: data.forcast.fixExpenseItems.map(i => ({...i, isArchieved: i.achieved, isClosed: i.closed})),
            variableExpenseItems: data.forcast.variableExpenseItems.map(i => ({...i, isArchieved: i.achieved, isClosed: i.closed})),
            achievedWishedItems: data.forcast.achievedWishedItems.map(i => ({...i, isArchieved: i.achieved, isClosed: i.closed})),
        } : undefined
    }
}

function toAllSpendingPeriod(data: GetAllSpendingPeriodResponse): AllSpendingPeriod {
    return {
        ...data,
        startDate: new Date(data.startDate),
        endDate: new Date(data.endDate),
        state: data.state as SpendingPeriodStateType
    }
}

export function toListSpendingPeriods(data: ListResponse<GetAllSpendingPeriodResponse>): List<AllSpendingPeriod> {
    return {
        items: data.items.map(i => toAllSpendingPeriod(i)),
        total: data.total
    }
}
