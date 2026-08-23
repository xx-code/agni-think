import type { Reactive } from "vue";
import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateCategoryRequest, GetCategoryResponse, UpdateCategoryRequest } from "~/types/api/category";
import { categoryResponseToCategory, listCategoriesResponseToListCategories } from "~/mappers/category";
import type { CategoryType } from "~/types/ui/category";
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder";
import { API_ROUTES } from "~/shared/routes";

export async function fetchCategories(query: Reactive<QueryFilterRequest & { isSystem?: boolean}>): Promise<ListResponse<CategoryType>> {
    return await ApiLinkBuilder
        .route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES)
        .query(query)
        .mapper(listCategoriesResponseToListCategories)
        .execute()
}

export async function fetchCategory(categoryId: string): Promise<CategoryType> {
    return await ApiLinkBuilder
        .route<GetCategoryResponse>(API_ROUTES.CATEGORIES.GET_CATEGORY)
        .params({ id: categoryId })
        .mapper(categoryResponseToCategory)
        .execute()
}

export async function useCreateCategory(request: CreateCategoryRequest): Promise<CreatedRequest> {
    return await ApiLinkBuilder
        .route<CreatedRequest>(API_ROUTES.CATEGORIES.CREATE_CATEGORY)
        .body(request)
        .execute()
}

export async function useDeleteCategory(categoryId: string): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.CATEGORIES.DELETE_CATEGORY)
        .params({ id: categoryId })
        .execute()
}

export async function useUpdateCategory(categoryId: string, request: UpdateCategoryRequest): Promise<void> {
    await ApiLinkBuilder
        .route(API_ROUTES.CATEGORIES.UPDATE_CATEGORY)
        .params({ id: categoryId })
        .body(request)
        .execute()
}
