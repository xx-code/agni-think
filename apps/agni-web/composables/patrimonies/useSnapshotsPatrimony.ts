import type { ListResponse } from "~/types/api";
import type { GetAllSnapshotPatrimonyRequest, GetAllSnapshotPatrimonyResponse } from "~/types/api/patrimony";
import type { SnapshotPatrimonyType } from "~/types/ui/patrimony";
import type { UseApiFetchReturn } from "~/types/utils";

export function useSnapshotsPatrimony(patrimonyId: string): UseApiFetchReturn<ListResponse<SnapshotPatrimonyType>> {
    const query: GetAllSnapshotPatrimonyRequest = {
        period: 'Month',
        periodTime: 1
    }
    const { data, error, refresh } = useFetch(`/api/patrimonies/${patrimonyId}/get-snapshots`, {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllSnapshotPatrimonyResponse>) => {
            return {
                items: data.items.map(i => ({ id: i.id, patrimonyId: i.patrimonyId, balance: i.balance, date: new Date(i.date), status: i.status})),
                totals: data.totals
            } satisfies ListResponse<SnapshotPatrimonyType>  
        }
    })

    return { data, error, refresh}
}

export async function fetchSnapshotsPatrimony(patrimonyId: string, startDate?: Date, endDate?: Date): Promise<ListResponse<SnapshotPatrimonyType>> {
    const query: GetAllSnapshotPatrimonyRequest = {
        period: 'Month',
        periodTime: 1
    }
    const res = await $fetch<ListResponse<GetAllSnapshotPatrimonyResponse>>(`/api/patrimonies/${patrimonyId}/get-snapshots`, {
        method: "GET",
        query: query 
    })

    return {
        items: res.items.map(i => ({ id: i.id, patrimonyId: i.patrimonyId, balance: i.balance, date: new Date(i.date), status: i.status})),
        totals: res.totals
    } satisfies ListResponse<SnapshotPatrimonyType>
}