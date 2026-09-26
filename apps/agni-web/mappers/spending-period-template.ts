import type { ListResponse } from "~/types/api";
import type { GetSpendingPeriodTemplateResponse } from "~/types/api/spending-period-template";
import type { List } from "~/types/ui";
import type { SpendingPeriodTemplate } from "~/types/ui/spending-period-template";

export function spendingPeriodTemplateResponseToSpendingPeriodTemplate(data: GetSpendingPeriodTemplateResponse): SpendingPeriodTemplate {
    return {
        ...data,
        isActive: data.active, 
        startDate: new Date(data.startDate),
        endDate: data.endDate ? new Date(data.endDate) : undefined 
    }
}

export function listSpendingPeriodTemplateResponseToListSpendingPeriodTemplate(data: ListResponse<GetSpendingPeriodTemplateResponse>): List<SpendingPeriodTemplate> {
    return {
        items: data.items.map(i => spendingPeriodTemplateResponseToSpendingPeriodTemplate(i)),
        total: data.total
    }
}