import type { CreatedRequest } from "~/types/api";
import type { CreateSaveGoalRequest } from "~/types/api/saveGoal";

export default async function useCreateSaveGoal(request: CreateSaveGoalRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`/api/save-goals`, {
        method: 'POST',
        body: request
    });

    return created;
}