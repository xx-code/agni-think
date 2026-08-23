import type { ListResponse } from "~/types/api";
import type { GetProvisionResponse } from "~/types/api/provision";
import type { ProvisionType } from "~/types/ui/provision";

export function provisionResponseToProvision(data: GetProvisionResponse): ProvisionType {
    return {
        id: data.id,
        title: data.title,
        initialCost: data.initialCost,
        acquisitionDate: new Date(data.acquisitionDate),
        expectedLifespanMonth: data.expectedLifespanMonth,
        residualValue: data.residualValue
    }
}

export function listProvisionsResponseToListProvisions(data: ListResponse<GetProvisionResponse>): ListResponse<ProvisionType> {
    return {
        items: data.items.map(i => provisionResponseToProvision(i)),
        total: data.total
    }
}
