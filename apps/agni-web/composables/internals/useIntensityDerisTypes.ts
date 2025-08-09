import type { GetIntensityDesirTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useIntensityDesirTypes(): UseApiFetchReturn<GetIntensityDesirTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetIntensityDesirTypeResponse[]>(`/api/internals/intensity-desir-types`, {
        method: 'GET'
    });

    return { data, error, refresh };
}