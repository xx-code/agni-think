import type { GetInternalTypeResponse } from "~/types/api/internal"

export async function fetchPeriodTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>('/api/internals/period-types', {
        method: 'GET'
    })
    return res
}