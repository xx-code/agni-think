import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useScheduleTransaction(scheduleTransactionId: string): UseApiFetchReturn<ScheduleInvoiceType> {
    const { data, error, refresh } = useFetch(`/api/schedule-transactions/${scheduleTransactionId}`, {
        method: 'GET',
        transform: (data: GetScheduleInvoiceResponse) => {
            return {
                id: data.id,
                accountId: data.accountId,
                amount: data.amount,
                categoryId: data.categoryId,
                isPause: data.isPause,
                name: data.name,
                isFreeze: data.isFreeze,
                tagIds: data.tagIds,
                type: data.type,
                repeater: data.repeater ? {
                    interval: data.repeater.interval,
                    period: data.repeater.periodType
                } : undefined,
                dueDate: new Date(data.dueDate) 
            } satisfies ScheduleInvoiceType
        }
    });

    return { data, error, refresh };
}

export async function fetchScheduleTransaction(scheduleTransactionId: string): Promise<ScheduleInvoiceType> {
    const res = await $fetch<GetScheduleInvoiceResponse>(`/api/schedule-transactions/${scheduleTransactionId}`, {
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