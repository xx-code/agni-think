import type {  GetBalanceResponse, QueryBalanceByPeriod, QueryInvoice } from "~/types/api/transaction";

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