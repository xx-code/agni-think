import type { FilterBalanceTransactionQuery, GetBalanceResponse } from "~/types/api/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useBalance(query: FilterBalanceTransactionQuery): UseApiFetchReturn<GetBalanceResponse> {
    const { data, error, refresh } = useFetch<GetBalanceResponse>(`/api/transactions/balance`, {
        method: "POST",
        body: query
    });


    return { data, error, refresh };
}