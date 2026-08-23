import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { AddRefundInternalRequest, CreateInternalLoanRequest, RemoveInternalRequestRequest, UpdateInternalLoanRequest } from "~/types/api/internal-loan";
import { internalLoanResponseToInternalLoan, listInternalLoansResponseToListInternalLoans } from "~/mappers/internalLoan";
import type { InternalLoanType } from "~/types/ui/internal-loan";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchInternalLoan(id: string): Promise<InternalLoanType> {
    return await ApiLinkBuilder
        .route(API_ROUTES.INTERNAL_LOANS.GET_INTERNAL_LOAN)
        .params({ id: id })
        .mapper(internalLoanResponseToInternalLoan)
        .execute()
}

export async function fetchInternalLoans(query: QueryFilterRequest): Promise<ListResponse<InternalLoanType>> {
    return await ApiLinkBuilder
        .route(API_ROUTES.INTERNAL_LOANS.GET_INTERNAL_LOANS)
        .query(query)
        .mapper(listInternalLoansResponseToListInternalLoans)
        .execute()
}

export async function useCreateInternalLoan(request: CreateInternalLoanRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.INTERNAL_LOANS.CREATE_INTERNAL_LOAN)
        .body(request)
        .execute()
}

export async function useUpdateInternalLoan(id: string, request: UpdateInternalLoanRequest) {
    await ApiLinkBuilder
        .route(API_ROUTES.INTERNAL_LOANS.UPDATE_INTERNAL_LOAN)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function useDeleteInternalLoan(id: string) {
    await ApiLinkBuilder
        .route(API_ROUTES.INTERNAL_LOANS.DELETE_INTERNAL_LOAN)
        .params({ id: id })
        .execute()
}

export async function useAddRefundInternalLoan(id: string, request: AddRefundInternalRequest) {
    await ApiLinkBuilder
        .route(API_ROUTES.INTERNAL_LOANS.ADD_FUND)
        .params({ id: id })
        .body(request)
        .execute()
}

export async function useRemoveRefundInternalLoan(id: string, request: RemoveInternalRequestRequest) {
    await ApiLinkBuilder
        .route(API_ROUTES.INTERNAL_LOANS.REMOVE_FUND)
        .params({ id: id })
        .body(request)
        .execute()
}
