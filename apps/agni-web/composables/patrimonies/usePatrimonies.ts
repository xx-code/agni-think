import type { ListResponse } from "~/types/api";
import type { GetAllPatrimoniesResponse, GetAllPatrimonyRequest } from "~/types/api/patrimony";
import type { PatrimonyType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function usePatrimonies(): UseApiFetchReturn<ListResponse<PatrimonyType>> {
    const query: GetAllPatrimonyRequest = {
        period: 'Month',
        periodTime: 1,
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const { data, refresh, error } = useFetch('/api/patrimonies', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllPatrimoniesResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    amount: i.amount,
                    title: i.title,
                    lastSnapshotBalance: i.lastSnapshotBalance,
                    currentBalance: i.currentSnapshotBalance,
                    type: i.type as TypePatrimony 
                })),
                totals: data.totals
            } satisfies ListResponse<PatrimonyType> 
        }
    })

    return { data, refresh, error}
}

export async function fetchPatrimonies(): Promise<ListResponse<PatrimonyType>> {
    const query: GetAllPatrimonyRequest = {
        period: 'Month',
        periodTime: 1,
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const res = await $fetch<ListResponse<GetAllPatrimoniesResponse>>('/api/patrimonies', {
        query: query,
        method: 'GET',
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            title: i.title,
            amount: i.amount,
            lastSnapshotBalance: i.lastSnapshotBalance,
            currentBalance: i.currentSnapshotBalance,
            type: i.type as TypePatrimony 
        })),
        totals: res.totals
    } 
}