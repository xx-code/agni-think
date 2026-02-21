import type { GetTagResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";

export async function fetchTag(tagId: string): Promise<TagType> {
    const res = await $fetch<GetTagResponse>(`/api/tags/${tagId}`, {
        method: 'GET',
    });

    return {
        id: res.id,
        color: res.color,
        value: res.value
    } satisfies TagType
} 