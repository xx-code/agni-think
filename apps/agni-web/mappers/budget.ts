import type { ListResponse } from "~/types/api";
import type { BudgetQueryFilterRequest, GetBudgetResponse } from "~/types/api/budget";
import type { BudgetCard, BudgetFilter, BudgetType } from "~/types/ui/budget";

export function budgetFilterToBudgetQueryRequest(filter: BudgetFilter): BudgetQueryFilterRequest {
    return {...filter, periodTypes: filter.periodTypes?.map(i => i.id)}
}

export function budgetResponseToBudget(data: GetBudgetResponse): BudgetType {
    return {
        id: data.id,
        title: data.title,
        currentBalance: data.currentBalance,
        target: data.target,
        dueDate: new Date(data.dueDate),
        repeater: data.repeater
    }
}

export function listBudgetsResponseToListBudgets(data: ListResponse<GetBudgetResponse>): ListResponse<BudgetType> {
    return {
        items: data.items.map(i => budgetResponseToBudget(i)),
        total: data.total
    }
}

export function budgetToBudgetCard(data: BudgetType): BudgetCard {
    return {...data, balance: data.currentBalance }
}