import type { BudgetQueryFilterRequest } from "~/types/api/budget";
import type { BudgetFilter } from "~/types/ui/budget";

export function budgetFilterToBudgetQueryRequest(filter: BudgetFilter): BudgetQueryFilterRequest {
    return {...filter, periodTypes: filter.periodTypes.map(i => i.id)}
}