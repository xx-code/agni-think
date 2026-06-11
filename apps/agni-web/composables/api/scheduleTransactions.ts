import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateScheduleInvoiceRequest, GetScheduleInvoiceResponse, UpdateScheduleInvoiceRequest } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";

export async function useCreateScheduleInvoice(request: CreateScheduleInvoiceRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/schedule-invoices`, {
        method: 'POST',
        body: request
    });

    return created;
}

export async function useDeleteScheduleInvoice(scheduleTransactionId: string): Promise<void> {
    await $fetch(`api/schedule-invoices/${scheduleTransactionId}`, {
        method: 'DELETE'
    });
}

export async function fetchScheduleInvoice(scheduleTransactionId: string): Promise<ScheduleInvoiceType> {
    const res = await $fetch<GetScheduleInvoiceResponse>(`api/schedule-invoices/${scheduleTransactionId}`, {
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

export async function fetchScheduleInvoices(query: QueryFilterRequest) : Promise<ListResponse<ScheduleInvoiceType>> {
    const data = await $fetch<ListResponse<GetScheduleInvoiceResponse>>(`api/schedule-invoices`, {
        method: 'GET',
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

export async function useUpdateScheduleInvoice(scheduleTransctionId: string, request: UpdateScheduleInvoiceRequest): Promise<void> {
    await $fetch(`api/schedule-invoices/${scheduleTransctionId}`, {
        method: "PUT",
        body: request 
    });
}