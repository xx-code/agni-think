import type { GetAccountTypeResponse, GetContributionTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useContributionTypes(): UseApiFetchReturn<GetContributionTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetContributionTypeResponse[]>('/api/internals/contribution-type', {
        method: 'GET'
    });

    return { data, error, refresh };
}

export async function fetchContributionTypes(): Promise<GetContributionTypeResponse[]> {
    const res = await $fetch<GetContributionTypeResponse[]>('/api/internals/contribution-type', {
        method: 'GET'
    })

    return res
}