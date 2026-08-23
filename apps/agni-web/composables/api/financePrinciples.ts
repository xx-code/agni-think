import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateFinancePrincipleRequest, GetFinancePrincipleResponse, UpdateFinancePrincipleRequest } from "~/types/api/financePrinciple";
import { financePrincipleResponseToFinancePrinciple, listFinancePrinciplesResponseToListFinancePrinciples } from "~/mappers/financePrinciple";
import type { FinancePrincipleType } from "~/types/ui/financePrinciple";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function createFinancePrinciple(request: CreateFinancePrincipleRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.FINANCE_PRINCIPLES.CREATE_FINANCE_PRINCIPLE)
        .body(request)
        .execute()
}

export async function updateFinancePrinciple(id: string, request: UpdateFinancePrincipleRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.FINANCE_PRINCIPLES.UPDATE_FINANCE_PRINCIPLE)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function deleteFinancePrinciple(id: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.FINANCE_PRINCIPLES.DELETE_FINANCE_PRINCIPLE)
        .params({ id: id })
        .execute()
}

export async function fetchFinancePrinciple(id: string): Promise<FinancePrincipleType> {
    return await ApiLinkBuilder
        .route<GetFinancePrincipleResponse>(API_ROUTES.FINANCE_PRINCIPLES.GET_FINANCE_PRINCIPLE)
        .params({ id: id })
        .mapper(financePrincipleResponseToFinancePrinciple)
        .execute()
}

export async function fetchFinancePrinciples(query: QueryFilterRequest): Promise<ListResponse<FinancePrincipleType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetFinancePrincipleResponse>>(API_ROUTES.FINANCE_PRINCIPLES.GET_FINANCE_PRINCIPLES)
        .query(query)
        .mapper(listFinancePrinciplesResponseToListFinancePrinciples)
        .execute()
}
