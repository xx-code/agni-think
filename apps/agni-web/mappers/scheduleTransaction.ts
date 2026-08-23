import type { ListResponse } from "~/types/api";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";

export function scheduleInvoiceResponseToScheduleInvoice(data: GetScheduleInvoiceResponse): ScheduleInvoiceType {
    return {
        id: data.id,
        name: data.name,
        accountId: data.accountId,
        categoryId: data.categoryId,
        tagIds: data.tagIds,
        type: data.type,
        amount: data.amount,
        isPause: data.isPause,
        isFreeze: data.isFreeze,
        repeater: data.repeater ? {
            interval: data.repeater.interval,
            period: data.repeater.periodType
        } : undefined,
        dueDate: new Date(data.dueDate)
    }
}

export function listScheduleInvoicesResponseToListScheduleInvoices(data: ListResponse<GetScheduleInvoiceResponse>): ListResponse<ScheduleInvoiceType> {
    return {
        items: data.items.map(i => scheduleInvoiceResponseToScheduleInvoice(i)),
        total: Number(data.total)
    }
}
