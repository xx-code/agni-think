import type { ListResponse } from "~/types/api";
import type { GetInvoiceResponse, QueryInvoice } from "~/types/api/transaction";
import type { InvoiceType } from "~/types/ui/transaction";

export async function fetchInvoicePagination(query: MaybeRefOrGetter<QueryInvoice>): Promise<ListResponse<InvoiceType>> {
    const res = await $fetch<ListResponse<GetInvoiceResponse>>('/api/invoices/pagination', {
        method: 'GET',
        query: query
    });

    return {
        items: res.items.map(i => ({
            id: i.id,
            accountId: i.accountId,
            date: new Date(i.date),
            type: i.type,
            status: i.status,
            mouvement: i.mouvement,
            total: i.total,
            subTotal: i.subTotal,
            transactions: i.transactions.map(i => ({
                id: i.id,
                amount: i.amount,
                description: i.description,
                budgetRefs: i.budgetIds,
                categoryId: i.categoryId,
                tagRefs: i.tagIds
            })),
            deductions: i.deductions.map(i => ({
                id: i.id,
                amount: i.amount
            })), 
        })),
        total: Number(res.total) 
    } satisfies ListResponse<InvoiceType>
}