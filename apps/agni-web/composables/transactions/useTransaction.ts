import type { GetTransactionResponse } from "~/types/api/transaction";
import type { TransactionType } from "~/types/ui/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTransaction(transactionId: string): UseApiFetchReturn<TransactionType> {
    const { data, error, refresh } = useFetch(`/api/transactions/${transactionId}`, {
        method: 'GET',
        transform: (data: GetTransactionResponse) => {
            return {
                id: data.id,
                accountId: data.accountId,
                date: new Date(data.date),
                type: data.type,
                status: data.status,
                total: data.totalAmount,
                subTotal: data.subTotalAmount,
                records: data.records.map(i => ({
                    id: i.id,
                    amount: i.amount,
                    type: i.type,
                    description: i.description,
                    budgetRefs: i.budgetRefs,
                    categoryId: i.categoryId,
                    tagRefs: i.tagRefs
                })),
                deductions: data.deductions.map(i => ({
                    id: i.id,
                    amount: i.amount
                })),
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
        id: res.id,
        accountId: res.accountId,
        date: new Date(res.date),
        type: res.type,
        status: res.status,
        total: res.totalAmount,
        subTotal: res.subTotalAmount,
        records: res.records.map(i => ({
            id: i.id,
            amount: i.amount,
            description: i.description,
            type: i.type,
            budgetRefs: i.budgetRefs,
            categoryId: i.categoryId,
            tagRefs: i.tagRefs
        })),
        deductions: res.deductions.map(i => ({
            id: i.id,
            amount: i.amount
        })),
    } satisfies TransactionType 
} 