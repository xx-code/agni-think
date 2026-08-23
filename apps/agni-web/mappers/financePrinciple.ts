import type { ListResponse } from "~/types/api";
import type { GetFinancePrincipleResponse } from "~/types/api/financePrinciple";
import type { FinancePrincipleType } from "~/types/ui/financePrinciple";

export function financePrincipleResponseToFinancePrinciple(data: GetFinancePrincipleResponse): FinancePrincipleType {
    return {
        id: data.id,
        name: data.name,
        description: data.description,
        targetType: data.targetType,
        strictness: data.strictness,
        logicRules: data.logicRules
    }
}

export function listFinancePrinciplesResponseToListFinancePrinciples(data: ListResponse<GetFinancePrincipleResponse>): ListResponse<FinancePrincipleType> {
    return {
        items: data.items.map(i => financePrincipleResponseToFinancePrinciple(i)),
        total: data.total
    }
}
