import type { ListResponse } from "~/types/api";
import type { GetAllPatrimoniesResponse } from "~/types/api/patrimony";
import type { PatrimonyType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function usePatrimonies(): UseApiFetchReturn<ListResponse<PatrimonyType>> {
    const { data, refresh, error } = useFetch('/api/patrimonies', {
        method: 'GET',
        transform: (data: ListResponse<GetAllPatrimoniesResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    title: i.title,
                    lastSnapshotBalance: i.lastBalance,
                    currentBalance: i.accountBalance,
                    type: i.type as TypePatrimony 
                })),
                totals: data.totals
            } satisfies ListResponse<PatrimonyType> 
        }
    })

    return { data, refresh, error}
}

export async function fetchPatrimonies(): Promise<ListResponse<PatrimonyType>> {
    const res = await $fetch<ListResponse<GetAllPatrimoniesResponse>>('/api/patrimonies', {
        method: 'GET'
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            title: i.title,
            lastSnapshotBalance: i.lastBalance,
            currentBalance: i.accountBalance,
            type: i.type as TypePatrimony 
        })),
        totals: res.totals
    } 
}