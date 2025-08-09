import type { CreatedRequest } from "~/types/api";
import type { CreateTransactionRequest } from "~/types/api/transaction";

export default async function useCreateTransaction(request: CreateTransactionRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/transactions', {
        method: 'POST',
        body: request
    });

    return created;
}