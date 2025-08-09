import type { ListResponse } from "~/types/api";
import type { GetAllTagsResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useTags(): UseApiFetchReturn<ListResponse<TagType>> {
    const { data, error, refresh } = useFetch('/api/tags', {
        method: 'GET',
        transform: (data: ListResponse<GetAllTagsResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    value: i.value,
                    color: i.color  
                })),
                totals: data.totals
            } satisfies ListResponse<TagType>
        }
    });

    return { data, error, refresh };
}