import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetDeductionResponse, RequestCreateDeduction, RequestUpdateDeduction } from "~/types/api/deduction";
import { deductionResponseToDeduction, listDeductionsResponseToListDeductions } from "~/mappers/deduction";
import type { DeductionType } from "~/types/ui/deduction";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchDeduction(id: string): Promise<DeductionType> {
    return await ApiLinkBuilder
        .route<GetDeductionResponse>(API_ROUTES.DEDUCTIONS.GET_DEDUCTION)
        .params({ id: id })
        .mapper(deductionResponseToDeduction)
        .execute()
}

export async function fetchDeductions(query: QueryFilterRequest): Promise<ListResponse<DeductionType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetDeductionResponse>>(API_ROUTES.DEDUCTIONS.GET_DEDUCTIONS)
        .query(query)
        .mapper(listDeductionsResponseToListDeductions)
        .execute()
}

export async function createDeduction(request: RequestCreateDeduction): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.DEDUCTIONS.CREATE_DEDUCTION)
        .body(request)
        .execute()
}

export async function updateDeduction(id: string, request: RequestUpdateDeduction): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.DEDUCTIONS.UPDATE_DEDUCTION)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function deleteDeduction(id: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.DEDUCTIONS.DELETE_DEDUCTION)
        .params({ id: id })
        .execute()
}
