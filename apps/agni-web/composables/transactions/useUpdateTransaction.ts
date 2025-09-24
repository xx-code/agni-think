import type { UpdateTransactionRequest } from "~/types/api/transaction";

export default async function useUpdateTransaction(transactionId: string, request: UpdateTransactionRequest): Promise<void> {
    console.log(request)
    await $fetch(`/api/transactions/${transactionId}`, {
        method: 'PUT',
        body: request
    })
}