import type { CreatedRequest, ListResponse } from "~/types/api";
import type { QueryFilterFundRequest, CreateFundRequest, DeleteFundRequest, GetFundResponse, UpdateFundRequest } from "~/types/api/fund";
import { fundResponseToFund } from "~/mappers/fund";
import type { Fund } from "~/types/ui/fund";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function useCreateFund(request: CreateFundRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.FUNDS.CREATE_FUND)
        .body(request)
        .execute()
}

export async function useDeleteFund(saveGoalId: string, request: DeleteFundRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.FUNDS.REMOVE_FUND)
        .params({ id: saveGoalId })
        .body(request)
        .execute()
}

export async function fetchFund(saveGoalId: string): Promise<Fund> {
    return await ApiLinkBuilder
        .route<GetFundResponse>(API_ROUTES.FUNDS.GET_FUND)
        .params({ id: saveGoalId })
        .mapper(fundResponseToFund)
        .execute()
}

export async function fetchFunds(query: QueryFilterFundRequest): Promise<ListResponse<Fund>> {
    const res = await ApiLinkBuilder
        .route<ListResponse<GetFundResponse>>(API_ROUTES.FUNDS.GET_FUNDS)
        .query(query)
        .execute()

    return {
        items: res.items.map(data => fundResponseToFund(data)),
        total: res.total
    }
}

export type UpdateFundAmountRequest = {
    fundId: string,
    isIncrease: boolean,
    amount: number,
    accountId: string
}
export async function useUpdateAmountFund(request: UpdateFundAmountRequest): Promise<void> {
    const body = {
        id: request.fundId,
        accountId: request.accountId,
        amount: request.amount
    }

    if (request.isIncrease) {
        await ApiLinkBuilder
            .route(API_ROUTES.FUNDS.INCREASE_FUND)
            .params({ id: request.fundId })
            .body(body)
            .execute()
    } else {
        await ApiLinkBuilder
            .route(API_ROUTES.FUNDS.DECREASE_FUND)
            .params({ id: request.fundId })
            .body(body)
            .execute()
    }
}

export async function useUpdateFund(saveGoalId: string, request: UpdateFundRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.FUNDS.UPDATE_FUND)
        .params({ id: saveGoalId })
        .body(request)
        .execute()
}
