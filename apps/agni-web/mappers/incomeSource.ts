import type { ListResponse } from "~/types/api";
import type { GetIncomeSourceResponse } from "~/types/api/incomeSource";
import type { IncomeSourceType } from "~/types/ui/incomeSource";

export function incomeSourceResponseToIncomeSource(data: GetIncomeSourceResponse): IncomeSourceType {
    return {
        id: data.id,
        title: data.title,
        type: data.type,
        payFrequencyType: data.payFrequencyType,
        reliabilityLevel: data.reliabilityLevel,
        taxRate: data.taxRate,
        otherRate: data.otherRate,
        startDate: new Date(data.startDate),
        endDate: data.endDate ? new Date(data.endDate) : undefined,
        linkedAccountId: data.linkedAccountId,
        annualGrossAmount: data.annualGrossAmount,
        estimatedFutureOccurrences: data.estimatedFutureOccurrences,
        estimateNextNetAmount: data.estimateNextNetAmount
    }
}

export function listIncomeSourcesResponseToListIncomeSources(data: ListResponse<GetIncomeSourceResponse>): ListResponse<IncomeSourceType> {
    return {
        items: data.items.map(i => incomeSourceResponseToIncomeSource(i)),
        total: data.total
    }
}
