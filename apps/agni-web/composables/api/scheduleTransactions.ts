import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateScheduleInvoiceRequest, GetScheduleInvoiceResponse, UpdateScheduleInvoiceRequest } from "~/types/api/scheduleTransaction";
import { listScheduleInvoicesResponseToListScheduleInvoices, scheduleInvoiceResponseToScheduleInvoice } from "~/mappers/scheduleTransaction";
import type { ScheduleInvoiceType } from "~/types/ui/scheduleTransaction";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function useCreateScheduleInvoice(request: CreateScheduleInvoiceRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.SCHEDULE_INVOICES.CREATE_SCHEDULE_INVOICE)
        .body(request)
        .execute()
}

export async function useDeleteScheduleInvoice(scheduleTransactionId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.SCHEDULE_INVOICES.DELETE_SCHEDULE_INVOICE)
        .params({ id: scheduleTransactionId })
        .execute()
}

export async function fetchScheduleInvoice(scheduleTransactionId: string): Promise<ScheduleInvoiceType> {
    return await ApiLinkBuilder
        .route<GetScheduleInvoiceResponse>(API_ROUTES.SCHEDULE_INVOICES.GET_SCHEDULE_INVOICE)
        .params({ id: scheduleTransactionId })
        .mapper(scheduleInvoiceResponseToScheduleInvoice)
        .execute()
}

export async function fetchScheduleInvoices(query: QueryFilterRequest): Promise<ListResponse<ScheduleInvoiceType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetScheduleInvoiceResponse>>(API_ROUTES.SCHEDULE_INVOICES.GET_SCHEDULE_INVOICES)
        .query(query)
        .mapper(listScheduleInvoicesResponseToListScheduleInvoices)
        .execute()
}

export async function useUpdateScheduleInvoice(scheduleTransctionId: string, request: UpdateScheduleInvoiceRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.SCHEDULE_INVOICES.UPDATE_SCHEDULE_INVOICE)
        .params({ id: scheduleTransctionId })
        .body(request)
        .execute()
}
