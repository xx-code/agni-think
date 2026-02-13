import type { GetInvoiceResponse } from "~/types/api/transaction";
import type { InvoiceType } from "~/types/ui/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useInvoice(transactionId: string): UseApiFetchReturn<InvoiceType> {
    const { data, error, refresh } = useFetch(`/api/invoices/${transactionId}`, {
        method: 'GET',
        transform: (data: GetInvoiceResponse) => {
            return {
                id: data.id,
                accountId: data.accountId,
                date: new Date(data.date),
                type: data.type,
                mouvement: data.mouvement,
                status: data.status,
                total: data.total,
                subTotal: data.subTotal,
                transactions: data.transactions.map(i => ({
                    id: i.id,
                    amount: i.amount,
                    description: i.description,
                    budgetRefs: i.budgetIds,
                    categoryId: i.categoryId,
                    tagRefs: i.tagIds
                })),
                deductions: data.deductions.map(i => ({
                    id: i.id,
                    amount: i.amount
                })),
            } satisfies InvoiceType
        }
    });

    return { data, error, refresh };
}

export async function fetchInvoice(transactionId: string): Promise<InvoiceType> {
    const res = await $fetch<GetInvoiceResponse>(`/api/invoices/${transactionId}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        accountId: res.accountId,
        date: new Date(res.date),
        type: res.type,
        status: res.status,
        mouvement: res.mouvement,
        total: res.total,
        subTotal: res.subTotal,
        transactions: res.transactions.map(i => ({
            id: i.id,
            amount: i.amount,
            description: i.description,
            budgetRefs: i.budgetIds,
            categoryId: i.categoryId,
            tagRefs: i.tagIds
        })),
        deductions: res.deductions.map(i => ({
            id: i.id,
            amount: i.amount
        })),
    } satisfies InvoiceType 
} 