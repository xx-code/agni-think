import type { Reactive } from "vue";
import type {  FilterTransactionQuery, GetBalanceResponse } from "~/types/api/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useBalance(query: Reactive<FilterTransactionQuery>): UseApiFetchReturn<GetBalanceResponse> {
    const { data, error, refresh } = useFetch<GetBalanceResponse>(`/api/transactions/balance`, {
        method: "POST",
        body: query
    });


    return { data, error, refresh };
}

export async function fetchBalance(query: FilterTransactionQuery): Promise<GetBalanceResponse> {
    const res = $fetch<GetBalanceResponse>(`/api/transactions/balance`, {
        method: "POST",
        body: query
    });


    return res;
}