import type { CreatedRequest } from "~/types/api";
import type { CreateScheduleInvoiceRequest } from "~/types/api/scheduleTransaction";

export default async function useCreateScheduleInvoice(request: CreateScheduleInvoiceRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/schedule-invoices', {
        method: 'POST',
        body: request
    });

    return created;
}