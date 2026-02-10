import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyType, TypePatrimony } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function usePatrimonies(): UseApiFetchReturn<ListResponse<PatrimonyType>> {
    const query: QueryFilterRequest = {
        // period: 'Month',
        // periodTime: 1,
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const { data, refresh, error } = useFetch('/api/patrimonies', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetPatrimonyResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    amount: i.amount,
                    title: i.title,
                    lastSnapshotBalance: i.pastBalance,
                    currentBalance: i.currentBalance,
                    type: i.type as TypePatrimony, 
                    accountIds: i.accountIds
                })),
                totals: Number(data.totals) 
            } satisfies ListResponse<PatrimonyType> 
        }
    })

    return { data, refresh, error}
}

export async function fetchPatrimonies(): Promise<ListResponse<PatrimonyType>> {
    const query: QueryFilterRequest = {
        // period: 'Month',
        // periodTime: 1,
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const res = await $fetch<ListResponse<GetPatrimonyResponse>>('/api/patrimonies', {
        query: query,
        method: 'GET',
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            title: i.title,
            amount: i.amount,
            lastSnapshotBalance: i.pastBalance,
            currentBalance: i.currentBalance,
            type: i.type as TypePatrimony,
            accountIds: i.accountIds
        })),
        totals: Number(res.totals) 
    } 
}