import type { GetEstimationLeftAmountRequest, GetEstimationLeftAmountResponse } from "~/types/api/analytics";
import type { EstimationLeftAmountType } from "~/types/ui/analytics";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useGetEstimationLeftAmount(request: GetEstimationLeftAmountRequest): UseApiFetchReturn<EstimationLeftAmountType>{
    const { data, error, refresh } = useFetch('/api/analytics/estimation-left-amount', {
        method: 'GET',
        query: request,
        transform: (data: GetEstimationLeftAmountResponse) => {
            return {
                estimateAmount: data.estimateAmount 
            } satisfies EstimationLeftAmountType
        }
    });

    return { data, error, refresh }
}

export async function fetchEstimationLeftAmount(request: GetEstimationLeftAmountRequest): Promise<EstimationLeftAmountType>{
    const response = await $fetch<GetEstimationLeftAmountResponse>('/api/analytics/estimation-left-amount', {
        method: 'GET',
        query: request 
    });

    return { estimateAmount: response.estimateAmount };
}