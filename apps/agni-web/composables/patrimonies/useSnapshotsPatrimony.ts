import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetSnapshotPatrimonyResponse } from "~/types/api/patrimony";
import type { SnapshotPatrimonyType } from "~/types/ui/patrimony";

export async function fetchSnapshotsPatrimony(patrimonyId: string, startDate?: Date, endDate?: Date): Promise<ListResponse<SnapshotPatrimonyType>> {
    const query: QueryFilterRequest = {
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const res = await $fetch<ListResponse<GetSnapshotPatrimonyResponse>>(`/api/patrimonies/${patrimonyId}/get-snapshots`, {
        method: "GET",
        query: query 
    })

    return {
        items: res.items.map(i => ({ id: i.id, patrimonyId: i.patrimonyId, balance: i.balance, date: new Date(i.date), status: i.status})),
        total: res.total
    } satisfies ListResponse<SnapshotPatrimonyType>
}