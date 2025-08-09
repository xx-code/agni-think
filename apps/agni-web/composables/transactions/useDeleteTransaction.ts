export default async function useDeleteTransaction(transactionId: string): Promise<void> {
    await $fetch(`/api/transactions/${transactionId}`, {
        method: 'DELETE'
    });
}