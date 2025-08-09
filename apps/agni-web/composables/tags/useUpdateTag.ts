import type { UpdateTagRequest } from "~/types/api/tag";

export default async function useUpdateTag(tagId: string, request: UpdateTagRequest): Promise<void> {
    await $fetch(`/api/tags/${tagId}`, {
        method: 'PUT',
        body: request
    })
}