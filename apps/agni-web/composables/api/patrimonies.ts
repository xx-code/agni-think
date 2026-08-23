import type { AddSnapshotPatrimonyRequest, CreatePatrimonyRequest, GetPatrimonyResponse, GetSnapshotPatrimonyResponse, UpdatePatrimonyRequest, UpdateSnapshotPatrimonyRequest } from "~/types/api/patrimony";
import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import { patrimonyResponseToPatrimony } from "~/mappers/patrimony";
import type { PatrimonyType, SnapshotPatrimonyType } from "~/types/ui/patrimony";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function useAddSnapshotPatrimony(patrimonyId: string, request: AddSnapshotPatrimonyRequest) {
    await ApiLinkBuilder
        .route(API_ROUTES.PATRIMONIES.ADD_SNAPSHOT)
        .params({ id: patrimonyId })
        .body(request)
        .execute()
}

export async function useCreatePatrimony(request: CreatePatrimonyRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.PATRIMONIES.CREATE_PATRIMONY)
        .body(request)
        .execute()
}

export async function useDeletePatrimony(patrimonyId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.PATRIMONIES.DELETE_PATRIMONY)
        .params({ id: patrimonyId })
        .execute()
}

export async function fetchPatrimonies(): Promise<ListResponse<PatrimonyType>> {
    const query: QueryFilterRequest = {
        limit: 0,
        offset: 0,
        queryAll: true
    }

    const res = await ApiLinkBuilder
        .route<ListResponse<GetPatrimonyResponse>>(API_ROUTES.PATRIMONIES.GET_PATRIMONIES)
        .query(query)
        .execute()

    return {
        items: res.items.map(i => patrimonyResponseToPatrimony(i)),
        total: Number(res.total)
    }
}

export async function fetchPatrimony(patrimonyId: string, isFund?: boolean): Promise<PatrimonyType> {
    if (isFund) {
        return await ApiLinkBuilder
            .route<GetPatrimonyResponse>(API_ROUTES.PATRIMONIES.TOTAL_FUND)
            .mapper(patrimonyResponseToPatrimony)
            .execute()
    }

    return await ApiLinkBuilder
        .route<GetPatrimonyResponse>(API_ROUTES.PATRIMONIES.GET_PATRIMONY)
        .params({ id: patrimonyId })
        .mapper(patrimonyResponseToPatrimony)
        .execute()
}

export async function useRemoveSnapshotPatrimony(patrimonyId: string, snapshotId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.PATRIMONIES.REMOVE_SNAPSHOT)
        .params({ id: snapshotId })
        .execute()
}

export async function fetchSnapshotsPatrimony(patrimonyId: string, startDate?: Date, endDate?: Date, isFund?: boolean): Promise<ListResponse<SnapshotPatrimonyType>> {
    const query = {
        limit: 0,
        offset: 0,
        queryAll: true,
        isFund: isFund
    }

    const res = await ApiLinkBuilder
        .route<ListResponse<GetSnapshotPatrimonyResponse>>(API_ROUTES.PATRIMONIES.GET_SNAPSHOTS)
        .params({ id: patrimonyId })
        .query(query)
        .execute()

    return {
        items: res.items.map(i => ({ id: i.id, patrimonyId: i.patrimonyId, balance: i.balance, date: new Date(i.date), status: i.status})),
        total: res.total
    } satisfies ListResponse<SnapshotPatrimonyType>
}

export async function useUpdatePatrimony(patrimonyId: string,
    request: UpdatePatrimonyRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.PATRIMONIES.UPDATE_PATRIMONY)
        .params({ id: patrimonyId })
        .body(request)
        .execute()
}

export async function useUpdateSnapshotPatrimony(patrimonyId: string, snapshotId: string,
    request: UpdateSnapshotPatrimonyRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.PATRIMONIES.UPDATE_SNAPSHOT)
        .params({ id: snapshotId })
        .body(request)
        .execute()
}
