import type { NuxtError } from "#app";
import type { Result } from "~/types";
import type { CreatedRequest, ErrorResponse, ListResponse, QueryFilterRequest } from "~/types/api";
import type {  CreateInvoiceRequest, FreezeInvoiceRequest, GetBalanceResponse, GetInvoiceResponse, QueryBalanceByPeriod, QueryInvoice, TransferInvoiceRequest, UpdateInvoiceRequest } from "~/types/api/transaction";
import type { InvoiceType } from "~/types/ui/transaction";

export async function fetchBalance(query: QueryInvoice): Promise<GetBalanceResponse> {
    const res = $fetch<GetBalanceResponse>(`api/invoices/balances`, {
        method: "GET",
        query: query
    });


    return res;
}

export async function fetchBalanceByPeriod(query: QueryBalanceByPeriod): Promise<GetBalanceResponse[]> {
    const res = $fetch<GetBalanceResponse[]>(`api/invoices/balances-by-period`, {
        method: 'GET',
        query: query
    })

    return res
}

export async function useCompleteInvoice(transactionId: string): Promise<void> {
    await $fetch(`api/invoices/${transactionId}/completed`, {
        method: 'PUT',
    });

}

export async function useCreateInvoice(request: CreateInvoiceRequest): Promise<Result<CreatedRequest>> {
    try {
        const created = await $fetch<CreatedRequest>(`api/invoices`, {
            method: 'POST',
            body: request
        });

        return { success: true, data: created } 
    } catch(err) {
        const nuxtError = err as NuxtError
        return { success: false, error: nuxtError.data as ErrorResponse}
    } 
}

export async function useDeleteInvoice(transactionId: string): Promise<void> {
    await $fetch(`api/invoices/${transactionId}`, {
        method: 'DELETE'
    });
}

export async function useFreezeInvoice(request: FreezeInvoiceRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/invoices/create-freeze`, {
        method: 'POST',
        body: request
    });

    return created;
}

export async function fetchInvoice(transactionId: string): Promise<InvoiceType> {
    const res = await $fetch<GetInvoiceResponse>(`api/invoices/${transactionId}`, {
        method: 'GET'
    });

    return {
        id: res.id,
        accountId: res.accountId,
        date: new Date(res.date),
        type: res.type,
        status: res.status,
        mouvement: res.mouvement,
        total: res.total,
        subTotal: res.subTotal,
        transactions: res.transactions.map(i => ({
            id: i.id,
            amount: i.amount,
            description: i.description,
            budgetRefs: i.budgetIds,
            categoryId: i.categoryId,
            tagRefs: i.tagIds
        })),
        deductions: res.deductions.map(i => ({
            id: i.id,
            amount: i.amount
        })),
    } satisfies InvoiceType 
} 

export async function fetchInvoicePagination(query: MaybeRefOrGetter<QueryFilterRequest & QueryInvoice>): Promise<ListResponse<InvoiceType>> {
    const res = await $fetch<ListResponse<GetInvoiceResponse>>(`api/invoices`, {
        method: 'GET',
        query: query
    });

    return {
        items: res.items.map(i => ({
            id: i.id,
            accountId: i.accountId,
            date: new Date(i.date),
            type: i.type,
            status: i.status,
            mouvement: i.mouvement,
            total: i.total,
            subTotal: i.subTotal,
            transactions: i.transactions.map(i => ({
                id: i.id,
                amount: i.amount,
                description: i.description,
                budgetRefs: i.budgetIds,
                categoryId: i.categoryId,
                tagRefs: i.tagIds
            })),
            deductions: i.deductions.map(i => ({
                id: i.id,
                amount: i.amount
            })), 
        })),
        total: Number(res.total) 
    } satisfies ListResponse<InvoiceType>
}

export async function useTransfertInvoice(request: TransferInvoiceRequest): Promise<void> {
    await $fetch(`api/invoices/transfert`, {
        method: "POST",
        body: request
    });
}

export async function useUpdateInvoice(invoiceId: string, request: UpdateInvoiceRequest): Promise<Result<void>> {
    try {
        await $fetch(`api/invoices/${invoiceId}`, {
            method: 'PUT',
            body: request
        })

        return { success: true }  
    } catch (err) {
        const nuxtError = err as NuxtError
        return { success: false, error: nuxtError.data as ErrorResponse}
    } 
}