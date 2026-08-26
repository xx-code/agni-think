import type { ListResponse } from "~/types/api";
import type { GetProvisionResponse } from "~/types/api/provision";
import type { Provision, } from "~/types/ui/provision";

export function provisionResponseToProvision(data: GetProvisionResponse): Provision {
    return {
        id: data.id,
        title: data.title,
        initialCost: data.initialCost,
        acquisitionDate: new Date(data.acquisitionDate),
        expectedLifespanMonth: data.expectedLifespanMonth,
        residualValue: data.residualValue,
        nextPaymentAmount: data.nextPaymentDate,
        nextPaymentDate: data.nextPaymentDate ? new Date(data.nextPaymentDate) : undefined
    }
}

export function listProvisionsResponseToListProvisions(data: ListResponse<GetProvisionResponse>): ListResponse<Provision> {
    return {
        items: data.items.map(i => provisionResponseToProvision(i)),
        total: data.total
    }
}
