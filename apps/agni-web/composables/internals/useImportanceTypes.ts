import type { GetImportanceGoalTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useImportanceTypes(): UseApiFetchReturn<GetImportanceGoalTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetImportanceGoalTypeResponse[]>(`/api/internals/importance-types`, {
        method: 'GET'
    });

    return { data, error, refresh };
}