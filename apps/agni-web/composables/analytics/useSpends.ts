import type { ListResponse } from "~/types/api";
import type { GetSpendCategoryRequest, GetSpendCategoryResponse, GetSpendTagRequest, GetSpendTagResponse } from "~/types/api/analytics";


export async function fetchSpendByCategoriesAnalytic(request: GetSpendCategoryRequest): Promise<ListResponse<GetSpendCategoryResponse>> {
    const response = await $fetch<ListResponse<GetSpendCategoryResponse>>('/api/analytics/spend-categories', {
        method: 'GET',
        query: request 
    });

    return response
}

export async function fetchSpendByTagAnalytic(request: GetSpendTagRequest): Promise<ListResponse<GetSpendTagResponse>> {
    const response = await $fetch<ListResponse<GetSpendTagResponse>>('/api/analytics/spend-tags', {
        method: 'GET',
        query: request 
    });

    return response
}