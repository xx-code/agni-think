import type { UpdateInvoiceRequest } from "~/types/api/transaction";

export default async function useUpdateInvoice(invoiceId: string, request: UpdateInvoiceRequest): Promise<void> {
    await $fetch(`/api/invoices/${invoiceId}`, {
        method: 'PUT',
        body: request
    })
}