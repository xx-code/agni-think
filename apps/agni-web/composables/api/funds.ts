import type { CreatedRequest, ListResponse } from "~/types/api";
import type { CreateFundRequest, GetFundResponse, QueryFilterFundRequest, UpdateFundRequest } from "~/types/api/fund";
import type { DeleteFundRequest } from "~/types/api/fund";
import type { FundType } from "~/types/ui/fund";

export async function useCreateFund(request: CreateFundRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/funds`, {
        method: 'POST',
        body: request
    });

    return created;
}


export async function useDeleteFund(saveGoalId: string, request: DeleteFundRequest): Promise<void> {
    await $fetch(`api/funds/${saveGoalId}`, {
        method: 'DELETE',
        body: request
    });
}

export async function fetchFund(saveGoalId: string): Promise<FundType> {
    const res = await $fetch<GetFundResponse>(`api/funds/${saveGoalId}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        title: res.title,
        description: res.description,
        balance: res.balance,
        target: res.target,
        accountId: res.accountId,
    };
}

export async function fetchFunds(query: QueryFilterFundRequest) : Promise<ListResponse<FundType>> {
    const res = await $fetch<ListResponse<GetFundResponse>>(`api/funds`, {
        method: 'GET',
        query: query
    })

    return {
        items: res.items.map(data => {
            return {
                id: data.id,
                title: data.title,
                description: data.description,
                target: data.target,
                balance: data.balance,
            } satisfies FundType
        }),
        total: res.total
    }
}

export type UpdateFundAmountRequest = {
    fundId: string, 
    isIncrease: boolean, 
    amount: number,
    accountId: string
}
export  async function useUpdateAmountFund(request: UpdateFundAmountRequest): Promise<void> {
    if (request.isIncrease)
        await $fetch(`api/funds/${request.fundId}/increase`, {
            method: "PUT",
            body: {
                id: request.fundId, 
                accountId: request.accountId,
                amount: request.amount 
            } 
        });    
    else 
        await $fetch(`api/funds/${request.fundId}/decrease`, {
            method: "PUT",
            body: {
                id: request.fundId,
                accountId: request.accountId,
                amount: request.amount
            }
        });
}

export async function useUpdateFund(saveGoalId: string, request: UpdateFundRequest): Promise<void> {
    await $fetch(`api/funds/${saveGoalId}`, {
        method: 'PUT',
        body: request
    }) 
}