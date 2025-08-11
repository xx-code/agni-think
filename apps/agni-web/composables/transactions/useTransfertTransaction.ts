import type { TransfertTransactionRequest } from "~/types/api/transaction";

export default async function useTransfertTransaction(request: TransfertTransactionRequest): Promise<void> {
    await $fetch('/api/transactions/transfert', {
        method: "POST",
        body: request
    });
}