import type { GetTransactionTypeResponse } from "~/types/api/internal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTransactionTypes(): UseApiFetchReturn<GetTransactionTypeResponse[]> {
    const { data, error, refresh } = useFetch<GetTransactionTypeResponse[]>('/api/internals/transaction-types', {
        method: 'GET'
    });

    return { data, error, refresh };
}