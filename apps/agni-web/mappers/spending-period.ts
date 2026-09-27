import type { ListResponse } from "~/types/api";
import type { GetAllSpendingPeriodResponse, GetSpendingPeriodResponse, GetSpendingPeriodStateResponse } from "~/types/api/spending-period";
import { SpendingPeriodType } from "~/types/constants/spendingPeriod";
import type { List } from "~/types/ui";
import type { AllSpendingPeriod, SpendingPeriod } from "~/types/ui/spending-period";

export function spendingPeriodStateResponseToState(data: GetSpendingPeriodStateResponse): SpendingPeriodType {
    const state = data.toLowerCase().replace('_', '')

    if (state === SpendingPeriodType.ToReview.toLowerCase())
        return SpendingPeriodType.ToReview

    if (state === SpendingPeriodType.InProgress.toLowerCase())
        return SpendingPeriodType.InProgress

    if (state === SpendingPeriodType.Complete.toLowerCase())
        return SpendingPeriodType.Complete

    return SpendingPeriodType.Draft
}

export function spendingPeriodResponseToSpendingPeriod(data: GetSpendingPeriodResponse): SpendingPeriod {
    return {
        ...data,
        startDate: new Date(data.startDate),
        endDate: new Date(data.endDate),
        state: spendingPeriodStateResponseToState(data.state)
    }
}

function getAllSpendingPeriodResponseToAllSpendingPeriod(data: GetAllSpendingPeriodResponse): AllSpendingPeriod {
    return {
        ...data,
        startDate: new Date(data.startDate),
        endDate: new Date(data.endDate),
        state: spendingPeriodStateResponseToState(data.state)
    }
}

export function listSpendingPeriodsResponseToListSpendingPeriods(data: ListResponse<GetAllSpendingPeriodResponse>): List<AllSpendingPeriod> {
    return {
        items: data.items.map(i => getAllSpendingPeriodResponseToAllSpendingPeriod(i)),
        total: data.total
    }
}
