import type { UpdateScheduleInvoiceRequest } from "~/types/api/scheduleTransaction";

export default async function useUpdateScheduleInvoice(scheduleTransctionId: string, request: UpdateScheduleInvoiceRequest): Promise<void> {
    await $fetch(`/api/schedule-invoices/${scheduleTransctionId}`, {
        method: "PUT",
        body: request 
    });
}