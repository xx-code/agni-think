import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useScheduleInvoices(query: Reactive<QueryFilterRequest>): UseApiFetchReturn<ListResponse<ScheduleInvoiceType>> {

    const { data, error, refresh } = useFetch('/api/schedule-invoices', {
        method: 'GET',
        query:query,
        transform: (data: ListResponse<GetScheduleInvoiceResponse>) => {
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
    })

    return { data, error, refresh };
}