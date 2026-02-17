import type { GetInternalTypeResponse } from "~/types/api/internal"

export async function fetchTransactionTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>('/api/internals/transaction-types', {
        method: 'GET'
    })
    return res
}