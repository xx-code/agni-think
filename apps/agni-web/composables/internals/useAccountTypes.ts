import type { GetAccountTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useAccountTypes(): UseApiFetchReturn<GetAccountTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetAccountTypeResponse[]>('/api/internals/account-types', {
        method: 'GET'
    });

    return { data, error, refresh };
}

export async function fetchAccountTypes(): Promise<GetAccountTypeResponse[]> {
    const res = await $fetch<GetAccountTypeResponse[]>('/api/internals/account-types', {
        method: 'GET'
    })

    return res
}