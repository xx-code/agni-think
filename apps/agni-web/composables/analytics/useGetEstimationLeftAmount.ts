import type { GetEstimationLeftAmountRequest, GetEstimationLeftAmountResponse } from "~/types/api/analytics";
import type { EstimationLeftAmountType } from "~/types/ui/analytics";

export async function fetchEstimationLeftAmount(request: GetEstimationLeftAmountRequest): Promise<EstimationLeftAmountType>{
    const response = await $fetch<GetEstimationLeftAmountResponse>('/api/analytics/estimation-left-amount', {
        method: 'GET',
        query: request 
    });

    return { estimateAmount: response.estimateAmount };
}