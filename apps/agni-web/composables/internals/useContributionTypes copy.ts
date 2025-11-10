import type { GetManagementTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useManagementAccountTypes(): UseApiFetchReturn<GetManagementTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetManagementTypeResponse[]>('/api/internals/management-account-type', {
        method: 'GET'
    });

    return { data, error, refresh };
}

export async function fetchManagementAccountTypes(): Promise<GetManagementTypeResponse[]> {
    const res = await $fetch<GetManagementTypeResponse[]>('/api/internals/management-account-type', {
        method: 'GET'
    })

    return res
}