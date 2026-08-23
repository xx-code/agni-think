import type { ListResponse } from "~/types/api";
import type { GetInvoiceResponse } from "~/types/api/transaction";
import type { InvoiceType } from "~/types/ui/transaction";

export function invoiceResponseToInvoice(data: GetInvoiceResponse): InvoiceType {
    return {
        id: data.id,
        accountId: data.accountId,
        date: new Date(data.date),
        type: data.type,
        status: data.status,
        mouvement: data.mouvement,
        total: data.total,
        subTotal: data.subTotal,
        transactions: data.transactions,
        deductions: data.deductions.map(i => ({
            id: i.id,
            amount: i.amount
        }))
    }
}

export function listInvoicesResponseToListInvoices(data: ListResponse<GetInvoiceResponse>): ListResponse<InvoiceType> {
    return {
        items: data.items.map(i => invoiceResponseToInvoice(i)),
        total: Number(data.total)
    }
}
