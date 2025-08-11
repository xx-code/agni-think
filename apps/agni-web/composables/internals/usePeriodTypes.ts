import type { GetPeriodTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function usePeriodTypes(): UseApiFetchReturn<GetPeriodTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetPeriodTypeResponse[]>(`/api/internals/period-types`, {
        method: 'GET'
    });

    return { data, error, refresh };
}