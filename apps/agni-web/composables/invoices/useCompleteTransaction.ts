
export default async function useCompleteInvoice(transactionId: string): Promise<void> {
    await $fetch(`/api/invoices/${transactionId}/complete`, {
        method: 'GET',
    });
}