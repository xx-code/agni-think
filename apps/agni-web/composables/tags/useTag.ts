import type { GetTagResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTag(tagId: string): UseApiFetchReturn<TagType> {
    const { data, error, refresh } = useFetch(`/api/tags/${tagId}`, {
        method: 'GET',
        transform: (data: GetTagResponse) => {
            return {
                id: data.id,
                color: data.color,
                value: data.value
            } satisfies TagType 
        }
    });

    return { data, error, refresh };
}

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