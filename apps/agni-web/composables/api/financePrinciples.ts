import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateFinancePrincipleRequest, GetFinancePrincipleResponse, UpdateFinancePrincipleRequest } from "~/types/api/financePrinciple";
import type { FinancePrincipleType } from "~/types/ui/financePrinciple";

export async function createFinancePrinciple(request: CreateFinancePrincipleRequest) : Promise<CreatedRequest> {
    return await $fetch(`${getApiBase()}/finance-principles`, {
        method: 'POST',
        body: request
    }) 
}

export async function updateFinancePrinciple(id: string, request: UpdateFinancePrincipleRequest) {
    await $fetch(`${getApiBase()}/finance-principles/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function deleteFinancePrinciple(id: string) {
    await $fetch(`${getApiBase()}/finance-principles/${id}`, {
        method: 'DELETE'
    })
}

export async function fetchFinancePrinciple(id: string) : Promise<FinancePrincipleType> {
    const data = await $fetch<GetFinancePrincipleResponse>(`${getApiBase()}/finance-principles/${id}`, {
        method: 'GET',
    })

    return {
       id: data.id, 
       description: data.description,
       name: data.name,
       strictness: data.strictness,
       targetType: data.targetType,
       logicRules: data.logicRules
    }
}

export async function fetchFinancePrinciples(query: QueryFilterRequest) : Promise<ListResponse<FinancePrincipleType>> {
    const data = await $fetch<ListResponse<GetFinancePrincipleResponse>>(`${getApiBase()}/finance-principles`, {
        method: 'GET',
        query: query
    })

    return  {
        items: data.items.map(data => {
            return {
                id: data.id, 
                description: data.description,
                name: data.name,
                strictness: data.strictness,
                targetType: data.targetType,
                logicRules: data.logicRules
            } satisfies GetFinancePrincipleResponse
        }),
        total: data.total
    }
}