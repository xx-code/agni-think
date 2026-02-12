import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetDeductionResponse, RequestCreateDeduction, RequestUpdateDeduction } from "~/types/api/deduction";
import type { DeductionType } from "~/types/ui/deduction";

export async function fetchDeduction(id: string): Promise<DeductionType> {
    const res = await $fetch<GetDeductionResponse>(`/api/deductions/${id}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        title: res.title,
        description: res.description,
        base: res.base,
        mode: res.mode
    } 
}

export async function fetchDeductions(query: QueryFilterRequest): Promise<ListResponse<DeductionType>> {
    const res = await $fetch<ListResponse<GetDeductionResponse>>(`/api/deductions`, {
        method: 'GET',
        query: query
    });

    return {
        items: res.items.map(i => ({
            id: i.id,
            description: i.description,
            title: i.title,
            base: i.base,
            mode: i.mode
        })),
        total: res.total
    }
}

export async function createDeduction(request: RequestCreateDeduction): Promise<CreatedRequest> {
    const res = await $fetch<CreatedRequest>('/api/deductions', {
        method: 'POST',
        body: request
    })

    return res
}

export async function updateDeduction(id: string, request: RequestUpdateDeduction): Promise<void> {
    await $fetch<CreatedRequest>(`/api/deductions/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function deleteDeduction(id: string): Promise<void> {
    await $fetch(`/api/deductions/${id}`, {
        method: 'DELETE'
    })
}