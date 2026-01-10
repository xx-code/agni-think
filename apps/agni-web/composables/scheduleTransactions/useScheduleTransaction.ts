import type { GetScheduleTransactionResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleTransactionType } from "~/types/ui/scheduleTransaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useScheduleTransaction(scheduleTransactionId: string): UseApiFetchReturn<ScheduleTransactionType> {
    const { data, error, refresh } = useFetch(`/api/schedule-transactions/${scheduleTransactionId}`, {
        method: 'GET',
        transform: (data: GetScheduleTransactionResponse) => {
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
                repeater: data.repeater,
                dueDate: new Date(data.dueDate) 
            } satisfies ScheduleTransactionType
        }
    });

    return { data, error, refresh };
}

export async function fetchScheduleTransaction(scheduleTransactionId: string): Promise<ScheduleTransactionType> {
    const res = await $fetch<GetScheduleTransactionResponse>(`/api/schedule-transactions/${scheduleTransactionId}`, {
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
            repeater: res.repeater,
            dueDate: new Date(res.dueDate)
        } 
}