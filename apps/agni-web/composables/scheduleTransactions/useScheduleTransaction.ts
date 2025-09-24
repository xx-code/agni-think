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
                dateStart: new Date(data.dateStart),
                dateUpdate: new Date(data.dateUpdate),
                isPause: data.isPause,
                name: data.name,
                period: data.period,
                isFreeze: data.isFreeze,
                periodTime: data.periodTime,
                tagIds: data.tagIds,
                type: data.type,
                dateEnd: data.dateEnd ? new Date(data.dateEnd) : undefined
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
            dateStart: new Date(res.dateStart),
            dateUpdate: new Date(res.dateUpdate),
            isPause: res.isPause,
            isFreeze: res.isFreeze,
            name: res.name,
            period: res.period,
            periodTime: res.periodTime,
            tagIds: res.tagIds,
            type: res.type,
            dateEnd: res.dateEnd ? new Date(res.dateEnd) : undefined
        } 
}