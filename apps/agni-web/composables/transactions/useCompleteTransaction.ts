
export default async function useCompleteTransaction(transactionId: string): Promise<void> {
    await $fetch(`/api/transactions/${transactionId}/complete`, {
        method: 'GET',
    });
}