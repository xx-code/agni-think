import type { GetImportanceGoalTypeResponse, GetInternalTypeResponse } from "~/types/api/internal";

export async function fetchImportanceTypes(): Promise<GetInternalTypeResponse[]> {
    const res = await $fetch<GetInternalTypeResponse[]>('/api/internals/importance-types', {
        method: 'GET'
    })
    return res
}