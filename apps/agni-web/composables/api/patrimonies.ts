import type { AddSnapshotPatrimonyRequest, CreatePatrimonyRequest, GetSnapshotPatrimonyResponse, UpdatePatrimonyRequest, UpdateSnapshotPatrimonyRequest } from "~/types/api/patrimony";
import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetPatrimonyResponse } from "~/types/api/patrimony";
import type { PatrimonyType, SnapshotPatrimonyType, TypePatrimony } from "~/types/ui/patrimony";

export async  function useAddSnapshotPatrimony(patrimonyId: string, request: AddSnapshotPatrimonyRequest) {
    await $fetch(`${getApiBase()}/patrimonies/${patrimonyId}/add-snapshot`, {
        method: "POST",
        body: request
    })
}

export async function useCreatePatrimony(request: CreatePatrimonyRequest): Promise<CreatedRequest> {
    const newPat = await $fetch<CreatedRequest>(`${getApiBase()}/patrimonies`, {
        method: 'POST',
        body: request
    })

    return newPat 
}

export async function useDeletePatrimony(patrimonyId: string): Promise<void> {
    await $fetch(`${getApiBase()}/patrimonies/${patrimonyId}`, {
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
    const res = await $fetch<ListResponse<GetPatrimonyResponse>>(`${getApiBase()}/patrimonies`, {
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
        total: Number(res.total) 
    } 
}

export async function fetchPatrimony(patrimonyId: string): Promise<PatrimonyType> {
    const res = await $fetch<GetPatrimonyResponse>(`${getApiBase()}/patrimonies/${patrimonyId}`, {
        method: 'GET'
    })

    return {
        id: res.id,
        title: res.title,
        amount: res.amount,
        currentBalance: res.currentBalance,
        lastSnapshotBalance: res.pastBalance,
        type: res.type as TypePatrimony,
        accountIds: res.accountIds
    }
}

export async function useRemoveSnapshotPatrimony(patrimonyId: string, snapshotId: string): Promise<void> {
    await $fetch(`${getApiBase()}/patrimonies/${patrimonyId}/remove-snapshot/${snapshotId}`, {
        method: 'PUT'
    });
}


export async function fetchSnapshotsPatrimony(patrimonyId: string, startDate?: Date, endDate?: Date): Promise<ListResponse<SnapshotPatrimonyType>> {
    const query: QueryFilterRequest = {
        limit: 0,
        offset: 0,
        queryAll: true
    }
    const res = await $fetch<ListResponse<GetSnapshotPatrimonyResponse>>(`${getApiBase()}/patrimonies/${patrimonyId}/snapshots`, {
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
    await $fetch(`${getApiBase()}/patrimonies/${patrimonyId}`, {
        method: 'PUT',
        body: request
    })
}

export async function useUpdateSnapshotPatrimony(patrimonyId: string, snapshotId: string, 
    request: UpdateSnapshotPatrimonyRequest): Promise<void> {
    await $fetch(`${getApiBase()}/patrimonies/${patrimonyId}/update-snapshot/${snapshotId}`, {
        method: 'PUT',
        body: request
    })
}