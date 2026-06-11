import type { CreatedRequest, ListResponse } from "~/types/api";
import type { CreateSavingGoalRequest, GetSavingGoalResponse, QueryFilterSavingGoalRequest, UpdateSavingGoalRequest } from "~/types/api/saveGoal";
import type { DeleteSavingGoalRequest } from "~/types/api/saveGoal";
import type { SaveGoalType } from "~/types/ui/saveGoal";

export async function useCreateSaveGoal(request: CreateSavingGoalRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/saving-goals`, {
        method: 'POST',
        body: request
    });

    return created;
}


export async function useDeleteSaveGoal(saveGoalId: string, request: DeleteSavingGoalRequest): Promise<void> {
    await $fetch(`api/saving-goals/${saveGoalId}`, {
        method: 'DELETE',
        body: request
    });
}

export async function fetchSaveGoal(saveGoalId: string): Promise<SaveGoalType> {
    const res = await $fetch<GetSavingGoalResponse>(`api/saving-goals/${saveGoalId}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        title: res.title,
        description: res.description,
        balance: res.balance,
        target: res.target,
        accountId: res.accountId,
        importance: res.importance,
        desirValue: res.desirValue,
        wishDueDate: res.wishDueDate ? new Date(res.wishDueDate) : undefined,
        items: res.items
    };
}

export async function fetchSavingGoals(query: QueryFilterSavingGoalRequest) : Promise<ListResponse<SaveGoalType>> {
    const res = await $fetch<ListResponse<GetSavingGoalResponse>>(`api/saving-goals`, {
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
                desirValue: data.desirValue,
                importance: data.importance,
                items: data.items.map(item => ({
                   title: item.title, 
                   url: item.url, 
                   price: item.price   
                }))
            } satisfies SaveGoalType
        }),
        total: res.total
    }
}

export type UpdateSaveGoalRequest = {
    saveGoalId: string, 
    isIncrease: boolean, 
    amount: number,
    accountId: string
}
export  async function useUpdateAmountSaveGoal(request: UpdateSaveGoalRequest): Promise<void> {
    if (request.isIncrease)
        await $fetch(`api/saving-goals/${request.saveGoalId}/increase`, {
            method: "PUT",
            body: {
                id: request.saveGoalId, 
                accountId: request.accountId,
                amount: request.amount 
            } 
        });    
    else 
        await $fetch(`api/saving-goals/${request.saveGoalId}/decrease`, {
            method: "PUT",
            body: {
                id: request.saveGoalId,
                accountId: request.accountId,
                amount: request.amount
            }
        });
}

export async function useUpdateSaveGaol(saveGoalId: string, request: UpdateSavingGoalRequest): Promise<void> {
    await $fetch(`api/saving-goals/${saveGoalId}`, {
        method: 'PUT',
        body: request
    }) 
}