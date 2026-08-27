import type { ListResponse } from "~/types/api";
import type { GetProvisionResponse } from "~/types/api/provision";
import type { Provision, ProvisionCard, ProvisionDepreciationCriteria } from "~/types/ui/provision";
import type { DepreciateType, ProvisionType } from "~/types/constants/provision";

export function provisionResponseToProvision(data: GetProvisionResponse): Provision {
    return {
        ...data,
        isPatrimony: data.patrimony,
        type: data.type as ProvisionType,
        acquisitionDate: new Date(data.acquisitionDate),
        depreciationCriteria: data.depreciationCriteria.map(c => ({
            ...c,
            type: c.type as DepreciateType
        })) as ProvisionDepreciationCriteria[],
    }
}

export function listProvisionsResponseToListProvisions(data: ListResponse<GetProvisionResponse>): ListResponse<Provision> {
    return {
        items: data.items.map(i => provisionResponseToProvision(i)),
        total: data.total
    }
}

export function provisionToProvisionCard(data: Provision): ProvisionCard {
    const now = new Date()
    const acq = data.acquisitionDate
    const elapsed = Math.max(0, (now.getFullYear() - acq.getFullYear()) * 12 + (now.getMonth() - acq.getMonth()))
    return {
        ...data, 
        amortizationPercent: Math.min(100, Math.round((elapsed / data.expectedLifespanMonth) * 100)),
        remainingMonths: Math.max(0, data.expectedLifespanMonth - elapsed)
    }
}
