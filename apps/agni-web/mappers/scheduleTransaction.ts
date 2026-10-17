import type { ListResponse } from "~/types/api";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoice } from "~/types/ui/scheduleTransaction";

export function scheduleInvoiceResponseToScheduleInvoice(data: GetScheduleInvoiceResponse): ScheduleInvoice {
    return {
        ...data,
        dueDate: new Date(data.dueDate),
        isPause: data.pause,
        isFreeze: data.type === 'Freeze',
        isEditable: data.editable,
        endDate: data.endDate ? new Date(data.endDate) : undefined,
        freezeEndDate: data.freezeEndDate ? new Date(data.freezeEndDate) : undefined
    }
}

export function listScheduleInvoicesResponseToListScheduleInvoices(data: ListResponse<GetScheduleInvoiceResponse>): ListResponse<ScheduleInvoice> {
    return {
        items: data.items.map(i => scheduleInvoiceResponseToScheduleInvoice(i)),
        total: Number(data.total)
    }
}
