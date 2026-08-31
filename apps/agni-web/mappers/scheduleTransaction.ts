import type { ListResponse } from "~/types/api";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";

export function scheduleInvoiceResponseToScheduleInvoice(data: GetScheduleInvoiceResponse): ScheduleInvoiceType {
    return {
        ...data,
        dueDate: new Date(data.dueDate),
        isFreeze: data.freeze,
        endDate: data.endDate ? new Date(data.endDate) : undefined,
        freezeEndDate: data.freezeEndDate ? new Date(data.freezeEndDate) : undefined
    }
}

export function listScheduleInvoicesResponseToListScheduleInvoices(data: ListResponse<GetScheduleInvoiceResponse>): ListResponse<ScheduleInvoiceType> {
    return {
        items: data.items.map(i => scheduleInvoiceResponseToScheduleInvoice(i)),
        total: Number(data.total)
    }
}
