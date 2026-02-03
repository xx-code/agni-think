import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetDeductionTypeResponse, RequestCreateDeductionType, RequestUpdateDeductionType } from "~/types/api/deduction";
import type { DeductionTypeType } from "~/types/ui/deduction";

export async function fetchDeductionType(id: string): Promise<DeductionTypeType> {
    const res = await $fetch<GetDeductionTypeResponse>(`/api/deduction-types/${id}`, {
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

export async function fetchDeductionTypes(query: QueryFilterRequest): Promise<ListResponse<DeductionTypeType>> {
    const res = await $fetch<ListResponse<DeductionTypeType>>(`/api/deduction-types`, {
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
        totals: res.totals
    }
}

export async function createDeductionType(request: RequestCreateDeductionType): Promise<CreatedRequest> {
    const res = await $fetch<CreatedRequest>('/api/deduction-types', {
        method: 'POST',
        body: request
    })

    return res
}

export async function updateDeductionType(id: string, request: RequestUpdateDeductionType): Promise<void> {
    await $fetch<CreatedRequest>(`/api/deduction-types/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function deleteDeductionType(id: string): Promise<void> {
    await $fetch(`/api/deduction-types/${id}`, {
        method: 'DELETE'
    })
}