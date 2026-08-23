import type { ListResponse } from "~/types/api";
import type { GetDeductionResponse } from "~/types/api/deduction";
import type { DeductionType } from "~/types/ui/deduction";

export function deductionResponseToDeduction(data: GetDeductionResponse): DeductionType {
    return {
        id: data.id,
        title: data.title,
        description: data.description,
        base: data.base,
        mode: data.mode
    }
}

export function listDeductionsResponseToListDeductions(data: ListResponse<GetDeductionResponse>): ListResponse<DeductionType> {
    return {
        items: data.items.map(i => deductionResponseToDeduction(i)),
        total: data.total
    }
}
