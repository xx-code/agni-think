import type { ListResponse } from "~/types/api";
import type { GetAllScheduleTransactionsResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleTransactionType } from "~/types/ui/scheduleTransaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useScheduleTransactions(): UseApiFetchReturn<ListResponse<ScheduleTransactionType>> {
    const { data, error, refresh } = useFetch('/api/schedule-transactions', {
        method: 'GET',
        transform: (data: ListResponse<GetAllScheduleTransactionsResponse>) => {
            return {
                items: data.items.map(i => (
                    {
                        id: i.id,
                        accountId: i.accountId,
                        amount: i.amount,
                        categoryId: i.categoryId,
                        dateStart: i.dateStart,
                        dateUpdate: i.dateUpdate,
                        isPause: i.isPause,
                        name: i.name,
                        period: i.period,
                        periodTime: i.periodTime,
                        tagIds: i.tagIds,
                        type: i.type,
                        dateEnd: i.dateEnd
                    }
                )),
                totals:data.totals 
            }  satisfies ListResponse<ScheduleTransactionType> 
        }
    })

    return { data, error, refresh };
}