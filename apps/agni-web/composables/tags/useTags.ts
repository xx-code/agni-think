import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetTagResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";

export async function fetchTags(query: QueryFilterRequest): Promise<ListResponse<TagType>> {

    const res = await $fetch<ListResponse<GetTagResponse>>('/api/tags', {
        method: 'GET',
        query: query
    });

    return { 
        items: res.items.map(i => ({
            id: i.id,
            value: i.value,
            color: i.color  
        } satisfies TagType)),
        total: Number(res.total)
     };
}