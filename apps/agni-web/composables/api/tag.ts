import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateTagRequest, GetTagResponse, UpdateTagRequest } from "~/types/api/tag";
import { listTagsResponseToListTags, tagResponseToTag } from "~/mappers/tag";
import type { TagType } from "~/types/ui/tag";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function useCreateTag(request: CreateTagRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.TAGS.CREATE_TAG)
        .body(request)
        .execute()
}

export async function useDeleteTag(tagId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.TAGS.DELETE_TAG)
        .params({ id: tagId })
        .execute()
}

export async function fetchTag(tagId: string): Promise<TagType> {
    return await ApiLinkBuilder
        .route<GetTagResponse>(API_ROUTES.TAGS.GET_TAG)
        .params({ id: tagId })
        .mapper(tagResponseToTag)
        .execute()
}

export async function fetchTags(query: QueryFilterRequest): Promise<ListResponse<TagType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS)
        .query(query)
        .mapper(listTagsResponseToListTags)
        .execute()
}

export async function useUpdateTag(tagId: string, request: UpdateTagRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.TAGS.UPDATE_TAG)
        .params({ id: tagId })
        .body(request)
        .execute()
}
