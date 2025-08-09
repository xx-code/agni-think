import type { CreatedRequest } from "~/types/api";
import type { CreateTagRequest } from "~/types/api/tag";

export default async function useCreateTag(request: CreateTagRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>('/api/tags', {
        method: 'POST',
        body: request
    });

    return created
}