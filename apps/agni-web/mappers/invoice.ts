import type { ListResponse } from "~/types/api";
import type { GetInvoiceResponse } from "~/types/api/transaction";
import type { InvoiceModuleLinkerType } from "~/types/constants/invoice";
import type { List } from "~/types/ui";
import type { InvoiceType } from "~/types/ui/transaction";

export function invoiceResponseToInvoice(data: GetInvoiceResponse): InvoiceType {
    return {
        ... data,
        isFreeze: data.freeze,
        date: new Date(data.date),
        moduleLinkers: data.moduleLinkers.map(i => ({
            sourecId: i.sourceId, 
            module: i.module as InvoiceModuleLinkerType 
        }))
    }
}

export function listInvoicesResponseToListInvoices(data: ListResponse<GetInvoiceResponse>): List<InvoiceType> {
    return {
        items: data.items.map(i => invoiceResponseToInvoice(i)),
        total: Number(data.total)
    }
}
