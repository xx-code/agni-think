import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateIncomeSourceRequest, GetIncomeSourceResponse, UpdateIncomeSourceRequest } from "~/types/api/incomeSource";
import { incomeSourceResponseToIncomeSource, listIncomeSourcesResponseToListIncomeSources } from "~/mappers/incomeSource";
import type { IncomeSourceType } from "~/types/ui/incomeSource";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function createIncomeSource(request: CreateIncomeSourceRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.INCOME_SOURCES.CREATE_INCOME_SOURCE)
        .body(request)
        .execute()
}

export async function updateIncomeSource(id: string, request: UpdateIncomeSourceRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.INCOME_SOURCES.UPDATE_INCOME_SOURCE)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function deleteIncomeSource(id: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.INCOME_SOURCES.DELETE_INCOME_SOURCE)
        .params({ id: id })
        .execute()
}

export async function fetchIncomeSource(id: string): Promise<IncomeSourceType> {
    return await ApiLinkBuilder
        .route<GetIncomeSourceResponse>(API_ROUTES.INCOME_SOURCES.GET_INCOME_SOURCE)
        .params({ id: id })
        .mapper(incomeSourceResponseToIncomeSource)
        .execute()
}

export async function fetchIncomeSources(query: QueryFilterRequest): Promise<ListResponse<IncomeSourceType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetIncomeSourceResponse>>(API_ROUTES.INCOME_SOURCES.GET_INCOME_SOURCES)
        .query(query)
        .mapper(listIncomeSourcesResponseToListIncomeSources)
        .execute()
}
