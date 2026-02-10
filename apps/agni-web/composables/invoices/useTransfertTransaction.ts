
export default async function useTransfertInvoice(request: TransfertRequest): Promise<void> {
    await $fetch('/api/invoices/transfert', {
        method: "POST",
        body: request
    });
}