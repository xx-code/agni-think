import type { TransferInvoiceRequest } from "~/types/api/transaction";

export default async function useTransfertInvoice(request: TransferInvoiceRequest): Promise<void> {
    await $fetch('/api/invoices/transfert', {
        method: "POST",
        body: request
    });
}