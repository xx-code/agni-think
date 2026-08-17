import type { BudgetQueryFilterRequest } from "~/types/api/budget";
import type { BudgetCard, BudgetFilter, BudgetType } from "~/types/ui/budget";

export function budgetFilterToBudgetQueryRequest(filter: BudgetFilter): BudgetQueryFilterRequest {
    return {...filter, periodTypes: filter.periodTypes?.map(i => i.id)}
}

export function budgetToBudgetCard(data: BudgetType): BudgetCard {
    return {...data, balance: data.currentBalance }
}