import type { UpdateTransactionRequest } from "~/types/api/transaction";

export default async function useUpdateTransaction(transactionId: string, request: UpdateTransactionRequest): Promise<void> {
    await $fetch(`/api/transactions/${transactionId}`, {
        method: 'PUT',
        body: request
    })
}