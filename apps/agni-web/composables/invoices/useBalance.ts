import type { Reactive } from "vue";
import type {  GetBalanceResponse, QueryBalanceByPeriod, QueryInvoice } from "~/types/api/transaction";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useBalance(query: Reactive<QueryInvoice>): UseApiFetchReturn<GetBalanceResponse> {
    const { data, error, refresh } = useFetch<GetBalanceResponse>(`/api/invoices/balance`, {
        method: "GET",
        body: query
    });


    return { data, error, refresh };
}

export async function fetchBalance(query: QueryInvoice): Promise<GetBalanceResponse> {
    const res = $fetch<GetBalanceResponse>(`/api/invoices/balance`, {
        method: "GET",
        query: query
    });


    return res;
}

export async function fetchBalanceByPeriod(query: QueryBalanceByPeriod): Promise<GetBalanceResponse[]> {
    const res = $fetch<GetBalanceResponse[]>(`/api/invoices/balances-by-period`, {
        method: 'GET',
        query: query
    })

    return res
}