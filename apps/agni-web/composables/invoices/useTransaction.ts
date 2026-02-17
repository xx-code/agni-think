import type { GetInvoiceResponse } from "~/types/api/transaction";
import type { InvoiceType } from "~/types/ui/transaction";

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