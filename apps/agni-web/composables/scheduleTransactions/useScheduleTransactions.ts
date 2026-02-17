import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";

export async function fetchScheduleInvoices(query: QueryFilterRequest) : Promise<ListResponse<ScheduleInvoiceType>> {
    const data = await $fetch<ListResponse<GetScheduleInvoiceResponse>>('/api/schedule-invoices', {
        body: 'GET',
        query: query
    })

    return {
        items: data.items.map(i => ({
                id: i.id,
                accountId: i.accountId,
                amount: i.amount,
                categoryId: i.categoryId,
                isPause: i.isPause,
                name: i.name,
                isFreeze: i.isFreeze,
                tagIds: i.tagIds,
                type: i.type,
                repeater: i.repeater ? {
                    interval: i.repeater.interval,
                    period: i.repeater.periodType
                } : undefined,
                dueDate: new Date(i.dueDate) 
            })),
        total: Number(data.total)  
    }  satisfies ListResponse<ScheduleInvoiceType> 
}