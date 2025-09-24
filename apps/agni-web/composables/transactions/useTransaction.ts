import type { GetTransactionResponse } from "~/types/api/transaction";
import type { TransactionType } from "~/types/ui/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTransaction(transactionId: string): UseApiFetchReturn<TransactionType> {
    const { data, error, refresh } = useFetch(`/api/transactions/${transactionId}`, {
        method: 'GET',
        transform: (data: GetTransactionResponse) => {
            return {
                id: data.transactionId,
                accountId: data.accountId,
                amount: data.amount,
                date: new Date(data.date),
                description: data.description,
                recordType: data.recordType,
                type: data.type,
                status: data.status,
                categoryId: data.categoryId,
                tagIds: data.tagRefs,
                budgetIds: data.budgets
            } satisfies TransactionType
        }
    });

    return { data, error, refresh };
}

export async function fetchTransaction(transactionId: string): Promise<TransactionType> {
    const res = await $fetch<GetTransactionResponse>(`/api/transactions/${transactionId}`, {
        method: 'GET'
    });

    return {
        id: res.transactionId,
        accountId: res.accountId,
        amount: res.amount,
        date: new Date(res.date),
        description: res.description,
        recordType: res.recordType,
        type: res.type,
        status: res.status,
        categoryId: res.categoryId,
        tagIds: res.tagRefs,
        budgetIds: res.budgets
    } satisfies TransactionType 
} 