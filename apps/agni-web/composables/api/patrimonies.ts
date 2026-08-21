import type { AddSnapshotPatrimonyRequest, CreatePatrimonyRequest, GetSnapshotPatrimonyResponse, UpdatePatrimonyRequest, UpdateSnapshotPatrimonyRequest } from "~/types/api/patrimony";
import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyType, SnapshotPatrimonyType } from "~/types/ui/patrimony";
import { patrimonyResponseToPatrimony } from "~/mappers/patrimony";

export async  function useAddSnapshotPatrimony(patrimonyId: string, request: AddSnapshotPatrimonyRequest) {
    await $fetch(`api/patrimonies/${patrimonyId}/add-snapshot`, {
        method: "POST",
        body: request
    })
}

export async function useCreatePatrimony(request: CreatePatrimonyRequest): Promise<CreatedRequest> {
    const newPat = await $fetch<CreatedRequest>(`api/patrimonies`, {
        method: 'POST',
        body: request
    })

    return newPat 
}

export async function useDeletePatrimony(patrimonyId: string): Promise<void> {
    await $fetch(`api/patrimonies/${patrimonyId}`, {
        method: 'DELETE'
    })
} 

export async function fetchPatrimonies(): Promise<ListResponse<PatrimonyType>> {
    const query: QueryFilterRequest = {
        // period: 'Month',
        // periodTime: 1,
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const res = await $fetch<ListResponse<GetPatrimonyResponse>>(`api/patrimonies`, {
        query: query,
        method: 'GET',
    })

    return {
        items: res.items.map(i => patrimonyResponseToPatrimony(i)),
        total: Number(res.total) 
    } 
}

export async function fetchPatrimony(patrimonyId: string, isFund?: boolean): Promise<PatrimonyType> {
    let patrimony = isFund ? await $fetch<GetPatrimonyResponse>(`/api/patrimonies/total-fund`) : 
        await $fetch<GetPatrimonyResponse>(`api/patrimonies/${patrimonyId}`)
        
    return patrimonyResponseToPatrimony(patrimony)
}

export async function useRemoveSnapshotPatrimony(patrimonyId: string, snapshotId: string): Promise<void> {
    await $fetch(`api/patrimonies/remove-snapshot/${snapshotId}`, {
        method: 'PUT'
    });
}


export async function fetchSnapshotsPatrimony(patrimonyId: string, startDate?: Date, endDate?: Date, isFund?: boolean): Promise<ListResponse<SnapshotPatrimonyType>> {
    const query = {
        limit: 0,
        offset: 0,
        queryAll: true,
        isFund: isFund
    }
    const res = await $fetch<ListResponse<GetSnapshotPatrimonyResponse>>(`api/patrimonies/${patrimonyId}/snapshots`, {
        method: "GET",
        query: query 
    })

    return {
        items: res.items.map(i => ({ id: i.id, patrimonyId: i.patrimonyId, balance: i.balance, date: new Date(i.date), status: i.status})),
        total: res.total
    } satisfies ListResponse<SnapshotPatrimonyType>
}

export async function useUpdatePatrimony(patrimonyId: string, 
    request: UpdatePatrimonyRequest): Promise<void> {
    await $fetch(`api/patrimonies/${patrimonyId}`, {
        method: 'PUT',
        body: request
    })
}

export async function useUpdateSnapshotPatrimony(patrimonyId: string, snapshotId: string, 
    request: UpdateSnapshotPatrimonyRequest): Promise<void> {
    await $fetch(`api/patrimonies/update-snapshot/${snapshotId}`, {
        method: 'PUT',
        body: request
    })
}