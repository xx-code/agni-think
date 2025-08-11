import type { UpdateScheduleTransactionRequest } from "~/types/api/scheduleTransaction";

export default async function useUpdateScheduleTransaction(scheduleTransctionId: string, request: UpdateScheduleTransactionRequest): Promise<void> {
    await $fetch(`/api/schedule-transactions/${scheduleTransctionId}`, {
        method: "PUT",
        body: request 
    });
}