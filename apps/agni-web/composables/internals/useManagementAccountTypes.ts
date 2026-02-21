import type { GetManagementTypeResponse } from "~/types/api/internal";

export async function fetchManagementAccountTypes(): Promise<GetManagementTypeResponse[]> {
    const res = await $fetch<GetManagementTypeResponse[]>('/api/internals/management-account-type', {
        method: 'GET'
    })

    return res
}