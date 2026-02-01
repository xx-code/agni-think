import type { Reactive } from "vue";
import type { ListResponse } from "~/types/api";
import type { FilterTransactionQuery, GetAllTransactionResponse } from "~/types/api/transaction";
import type { TransactionType } from "~/types/ui/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTransactionPagination(query: Reactive<FilterTransactionQuery>): UseApiFetchReturn<ListResponse<TransactionType>> {

    const { data, error, refresh } = useFetch('/api/transactions/pagination', {
        method: 'POST',
        body: query,
        transform: (data: ListResponse<GetAllTransactionResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    accountId: i.accountId,
                    date: new Date(i.date),
                    type: i.type,
                    status: i.status,
                    total: i.totalAmount,
                    subTotal: i.subTotalAmount,
                    records: i.records.map(i => ({
                        id: i.id,
                        amount: i.amount,
                        type: i.type,
                        description: i.description, 
                        budgetRefs: i.budgetRefs,
                        categoryId: i.categoryId,
                        tagRefs: i.tagRefs
                    })),
                    deductions: i.deductions.map(i => ({
                        id: i.id,
                        amount: i.amount
                    })),
                })),
                totals: Number(data.totals) 
            } satisfies ListResponse<TransactionType>
        }
    });

    return { data, error, refresh };
}

export async function fetchTransactionPagination(query: MaybeRefOrGetter<FilterTransactionQuery>): Promise<ListResponse<TransactionType>> {
    const res = await $fetch<ListResponse<GetAllTransactionResponse>>('/api/transactions/pagination', {
        method: 'POST',
        body: query
    });

    return {
        items: res.items.map(i => ({
            id: i.id,
            accountId: i.accountId,
            date: new Date(i.date),
            type: i.type,
            status: i.status,
            total: i.totalAmount,
            subTotal: i.subTotalAmount,
            records: i.records.map(i => ({
                id: i.id,
                amount: i.amount,
                description: i.description,
                type: i.type,
                budgetRefs: i.budgetRefs,
                categoryId: i.categoryId,
                tagRefs: i.tagRefs
            })),
            deductions: i.deductions.map(i => ({
                id: i.id,
                amount: i.amount
            })), 
        })),
        totals: Number(res.totals) 
    } satisfies ListResponse<TransactionType>
}