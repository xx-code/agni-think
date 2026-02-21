import type { GetAccountTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export async function fetchAccountTypes(): Promise<GetAccountTypeResponse[]> {
    const res = await $fetch<GetAccountTypeResponse[]>('/api/internals/account-types', {
        method: 'GET'
    })

    return res
}