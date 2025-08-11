import type { CreatedRequest } from "~/types/api";
import type { FreezeTransactionRequest } from "~/types/api/transaction";

export default async function useFreezeTransaction(request: FreezeTransactionRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/transactions/freeze', {
        method: 'POST',
        body: request
    });

    return created;
}