import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetAllTagsResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTags(query: Reactive<QueryFilterRequest>): UseApiFetchReturn<ListResponse<TagType>> {

    const { data, error, refresh } = useFetch('/api/tags', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllTagsResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    value: i.value,
                    color: i.color  
                })),
                totals: Number(data.totals) 
            } satisfies ListResponse<TagType>
        }
    });

    return { data, error, refresh };
}

export async function fetchTags(query: QueryFilterRequest): Promise<ListResponse<TagType>> {

    const res = await $fetch<ListResponse<GetAllTagsResponse>>('/api/tags', {
        method: 'GET',
        query: query
    });

    return { 
        items: res.items.map(i => ({
            id: i.id,
            value: i.value,
            color: i.color  
        } satisfies TagType)),
        totals: Number(res.totals)
     };
}