import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateTagRequest, UpdateTagRequest } from "~/types/api/tag";
import type { GetTagResponse } from "~/types/api/tag";
import type { TagType } from "~/types/ui/tag";

export async function useCreateTag(request: CreateTagRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/tags`, {
        method: 'POST',
        body: request
    });

    return created
}

export async function useDeleteTag(tagId: string): Promise<void> {
    await $fetch(`api/tags/${tagId}`, {
        method: 'DELETE',
    });
}



export async function fetchTag(tagId: string): Promise<TagType> {
    const res = await $fetch<GetTagResponse>(`api/tags/${tagId}`, {
        method: 'GET',
    });

    return {
        id: res.id,
        color: res.color,
        value: res.value
    } satisfies TagType
} 

export async function fetchTags(query: QueryFilterRequest): Promise<ListResponse<TagType>> {

    const res = await $fetch<ListResponse<GetTagResponse>>(`api/tags`, {
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

export async function useUpdateTag(tagId: string, request: UpdateTagRequest): Promise<void> {
    await $fetch(`api/tags/${tagId}`, {
        method: 'PUT',
        body: request
    })
}