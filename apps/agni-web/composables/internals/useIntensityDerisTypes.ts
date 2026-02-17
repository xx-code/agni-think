import type { GetInternalTypeResponse } from "~/types/api/internal"

export async function fetcheIntensityDesirTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>('/api/internals/intensity-desir-types', {
        method: 'GET'
    })
    return res
}