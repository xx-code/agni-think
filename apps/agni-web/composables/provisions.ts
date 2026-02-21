import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateProvisionRequest, GetProvisionResponse, UpdateProvisionRequest } from "~/types/api/provision";
import type { ProvisionType } from "~/types/ui/provision";

export async function createProvision(request: CreateProvisionRequest) : Promise<CreatedRequest> {
    return await $fetch(`/api/provisions`, {
        method: 'POST',
        body: request
    }) 
}

export async function updateProvision(id: string, request: UpdateProvisionRequest) {
    await $fetch(`/api/provisions/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function deleteProvision(id: string) {
    await $fetch(`/api/provisions/${id}`)
}

export async function fetchProvision(id: string) : Promise<ProvisionType> {
    const data = await $fetch<GetProvisionResponse>(`/api/provisions/${id}`)

    return {
        id: data.id,
        acquisitionDate: new Date(data.acquisitionDate),
        expectedLifespanMonth: data.expectedLifespanMonth,
        initialCost: data.initialCost,
        residualValue: data.residualValue,
        title: data.title
    }
}

export async function fetchProvisions(query: QueryFilterRequest) : Promise<ListResponse<ProvisionType>> {
    const data = await $fetch<ListResponse<GetProvisionResponse>>(`/api/provisions`, {
        method: 'GET',
        query: query
    })

    return {
        items: data.items.map(data => {
            return {
                id: data.id,
                acquisitionDate: new Date(data.acquisitionDate),
                expectedLifespanMonth: data.expectedLifespanMonth,
                initialCost: data.initialCost,
                residualValue: data.residualValue,
                title: data.title
            } satisfies ProvisionType
        }),
        total: data.total
    }
}