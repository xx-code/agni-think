import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateProvisionRequest, GetProvisionResponse, UpdateProvisionRequest } from "~/types/api/provision";
import { listProvisionsResponseToListProvisions, provisionResponseToProvision } from "~/mappers/provision";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";
import type { Provision } from "~/types/ui/provision";

export async function createProvision(request: CreateProvisionRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.PROVISIONS.CREATE_PROVISION)
        .body(request)
        .execute()
}

export async function updateProvision(id: string, request: UpdateProvisionRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.PROVISIONS.UPDATE_PROVISION)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function deleteProvision(id: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.PROVISIONS.DELETE_PROVISION)
        .method('DELETE')
        .params({ id: id })
        .execute()
}

export async function fetchProvision(id: string): Promise<Provision> {
    return await ApiLinkBuilder
        .route<GetProvisionResponse>(API_ROUTES.PROVISIONS.GET_PROVISION)
        .params({ id: id })
        .mapper(provisionResponseToProvision)
        .execute()
}

export async function fetchProvisions(query: QueryFilterRequest): Promise<ListResponse<Provision>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetProvisionResponse>>(API_ROUTES.PROVISIONS.GET_PROVISIONS)
        .query(query)
        .mapper(listProvisionsResponseToListProvisions)
        .execute()
}
