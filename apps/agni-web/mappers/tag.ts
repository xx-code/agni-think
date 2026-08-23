import type { ListResponse } from "~/types/api";
import type { GetTagResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";

export function tagResponseToTag(data: GetTagResponse): TagType {
    return {
        id: data.id,
        value: data.value,
        color: data.color
    }
}

export function listTagsResponseToListTags(data: ListResponse<GetTagResponse>): ListResponse<TagType> {
    return {
        items: data.items.map(i => tagResponseToTag(i)),
        total: Number(data.total)
    }
}
