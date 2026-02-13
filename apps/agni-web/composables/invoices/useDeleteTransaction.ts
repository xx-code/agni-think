export default async function useDeleteInvoice(transactionId: string): Promise<void> {
    await $fetch(`/api/invoices/${transactionId}`, {
        method: 'DELETE'
    });
}