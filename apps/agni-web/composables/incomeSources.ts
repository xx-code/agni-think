import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateIncomeSourceRequest, GetIncomeSourceResponse, UpdateIncomeSourceRequest } from "~/types/api/incomeSource";
import type { IncomeSourceType } from "~/types/ui/incomeSource";

export async function createIncomeSource(request: CreateIncomeSourceRequest) : Promise<CreatedRequest> {
    return await $fetch('/api/income-sources', {
        method: 'POST',
        body: request
    })
}

export async function updateIncomeSource(id: string, request: UpdateIncomeSourceRequest) {
    await $fetch(`/api/income-sources/${id}`, {
        method: 'PUT',
        body: request
    })
}

export async function deleteIncomeSource(id: string) {
    await $fetch(`/api/income-sources/${id}`, {
        method: 'DELETE'
    })
}

export async function fetchIncomeSource(id: string) : Promise<IncomeSourceType> {
    const data = await $fetch<GetIncomeSourceResponse>(`/api/income-sources/${id}`, {
        method: 'GET'
    })

    return {
        id: data.id,
        title: data.title,
        payFrequencyType: data.payFrequencyType,
        startDate: new Date(data.startDate), 
        linkedAccountId: data.linkedAccountId,
        estimateNextNetAmount: data.estimateNextNetAmount,
        endDate: data.endDate ? new Date(data.endDate) : undefined,
        annualGrossAmount: data.annualGrossAmount,
        type: data.type,
        taxRate: data.taxRate,
        reliabilityLevel: data.reliabilityLevel,
        otherRate: data.otherRate,
        estimatedFutureOccurrences: data.estimatedFutureOccurrences
    }
}

export async function fetchIncomeSources(query: QueryFilterRequest) : Promise<ListResponse<IncomeSourceType>> {
    const data = await $fetch<ListResponse<GetIncomeSourceResponse>>(`/api/income-sources`, {
        method: 'GET',
        query: query
    })

    return {
        items: data.items.map(data => {
            return {
                id: data.id,
                linkedAccountId: data.linkedAccountId,
                estimateNextNetAmount: data.estimateNextNetAmount,
                endDate: data.endDate ? new Date(data.endDate) : undefined,
                annualGrossAmount: data.annualGrossAmount,
                type: data.type,
                taxRate: data.taxRate,
                reliabilityLevel: data.reliabilityLevel,
                otherRate: data.otherRate,
                estimatedFutureOccurrences: data.estimatedFutureOccurrences,
                title: data.title,
                startDate: new Date(data.startDate),
                payFrequencyType: data.payFrequencyType
            } satisfies IncomeSourceType
        }),
        total: data.total
    } 
}