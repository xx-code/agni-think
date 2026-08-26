import type { ListResponse } from "~/types/api";
import type { GetProvisionResponse } from "~/types/api/provision";
import type { Provision, ProvisionCard, } from "~/types/ui/provision";

export function provisionResponseToProvision(data: GetProvisionResponse): Provision {
    return {
        ...data,
        acquisitionDate: new Date(data.acquisitionDate),
        nextPaymentDate: data.nextPaymentDate ? new Date(data.nextPaymentDate) : undefined
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
