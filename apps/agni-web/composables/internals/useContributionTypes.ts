import type { GetContributionTypeResponse } from "~/types/api/internal";

export async function fetchContributionTypes(): Promise<GetContributionTypeResponse[]> {
    const res = await $fetch<GetContributionTypeResponse[]>('/api/internals/contribution-type', {
        method: 'GET'
    })

    return res
}