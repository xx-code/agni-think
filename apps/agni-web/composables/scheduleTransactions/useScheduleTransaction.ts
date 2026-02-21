import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";

export async function fetchScheduleInvoice(scheduleTransactionId: string): Promise<ScheduleInvoiceType> {
    const res = await $fetch<GetScheduleInvoiceResponse>(`/api/schedule-invoices/${scheduleTransactionId}`, {
        method: 'GET'
    });

    return {
            id: res.id,
            accountId: res.accountId,
            amount: res.amount,
            categoryId: res.categoryId,
            isPause: res.isPause,
            isFreeze: res.isFreeze,
            name: res.name,
            tagIds: res.tagIds,
            type: res.type,
            repeater: res.repeater ? {
                interval: res.repeater.interval,
                period: res.repeater.periodType
            } : undefined,
            dueDate: new Date(res.dueDate)
        } 
}