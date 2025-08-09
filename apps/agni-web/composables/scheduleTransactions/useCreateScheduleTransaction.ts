import type { CreatedRequest } from "~/types/api";
import type { CreateScheduleTransactionRequest } from "~/types/api/scheduleTransaction";

export default async function useCreateScheduleTransaction(request: CreateScheduleTransactionRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/schedule-transactions', {
        method: 'POST',
        body: request
    });

    return created;
}