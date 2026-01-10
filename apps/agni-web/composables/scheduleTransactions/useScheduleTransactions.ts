import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAllScheduleTransactionsResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleTransactionType } from "~/types/ui/scheduleTransaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useScheduleTransactions(query: Reactive<QueryFilterRequest>): UseApiFetchReturn<ListResponse<ScheduleTransactionType>> {

    const { data, error, refresh } = useFetch('/api/schedule-transactions', {
        method: 'GET',
        query:query,
        transform: (data: ListResponse<GetAllScheduleTransactionsResponse>) => {
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
                    repeater: i.repeater,
                    dueDate: new Date(i.dueDate) 
                })),
                totals: Number(data.totals)  
            }  satisfies ListResponse<ScheduleTransactionType> 
        }
    })

    return { data, error, refresh };
}