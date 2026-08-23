import type { NuxtError } from "#app";
import type { Result } from "~/types";
import type { CreatedRequest, ErrorResponse, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateInvoiceRequest, FreezeInvoiceRequest, GetBalanceResponse, GetInvoiceResponse, QueryBalanceByPeriod, QueryInvoice, TransferInvoiceRequest, UpdateInvoiceRequest } from "~/types/api/transaction";
import { invoiceResponseToInvoice, listInvoicesResponseToListInvoices } from "~/mappers/invoice";
import type { InvoiceType } from "~/types/ui/transaction";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchBalance(query: QueryInvoice): Promise<GetBalanceResponse> {
    return await ApiLinkBuilder
        .route<GetBalanceResponse>(API_ROUTES.INVOICES.GET_BALANCES)
        .query(query)
        .execute()
}

export async function fetchBalanceByPeriod(query: QueryBalanceByPeriod): Promise<GetBalanceResponse[]> {
    return await ApiLinkBuilder
        .route<GetBalanceResponse[]>(API_ROUTES.INVOICES.GET_BALANCES_BY_PERIOD)
        .query(query)
        .execute()
}

export async function useCompleteInvoice(transactionId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.INVOICES.COMPLETE_INVOICE)
        .params({ id: transactionId })
        .execute()
}

export async function useCreateInvoice(request: CreateInvoiceRequest): Promise<Result<CreatedRequest>> {
    try {
        const created = await ApiLinkBuilder
            .route<CreatedRequest>(API_ROUTES.INVOICES.CREATE_INVOICE)
            .body(request)
            .execute()

        return { success: true, data: created }
    } catch(err) {
        const nuxtError = err as NuxtError
        return { success: false, error: nuxtError.data as ErrorResponse}
    }
}

export async function useDeleteInvoice(transactionId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.INVOICES.DELETE_INVOICE)
        .params({ id: transactionId })
        .execute()
}

export async function useFreezeInvoice(request: FreezeInvoiceRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.INVOICES.CREATE_FREEZE)
        .body(request)
        .execute()
}

export async function fetchInvoice(transactionId: string): Promise<InvoiceType> {
    return await ApiLinkBuilder
        .route<GetInvoiceResponse>(API_ROUTES.INVOICES.GET_INVOICE)
        .params({ id: transactionId })
        .mapper(invoiceResponseToInvoice)
        .execute()
}

export async function fetchInvoicePagination(query: MaybeRefOrGetter<QueryFilterRequest & QueryInvoice>): Promise<ListResponse<InvoiceType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetInvoiceResponse>>(API_ROUTES.INVOICES.GET_INVOICES)
        .query(query)
        .mapper(listInvoicesResponseToListInvoices)
        .execute()
}

export async function useTransfertInvoice(request: TransferInvoiceRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.INVOICES.TRANSFER)
        .body(request)
        .execute()
}

export async function useUpdateInvoice(invoiceId: string, request: UpdateInvoiceRequest): Promise<Result<void>> {
    try {
        await ApiLinkBuilder
            .route(API_ROUTES.INVOICES.UPDATE_INVOICE)
            .params({ id: invoiceId })
            .body(request)
            .execute()

        return { success: true }
    } catch (err) {
        const nuxtError = err as NuxtError
        return { success: false, error: nuxtError.data as ErrorResponse}
    }
}
