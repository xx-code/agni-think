import type { Reactive } from "vue";
import type { CreatedRequest, ListResponse, QueryFilterRequest } from "~/types/api";
import type { CreateCategoryRequest, GetCategoryResponse, UpdateCategoryRequest } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";

export async function fetchCategories(query: Reactive<QueryFilterRequest & { isSystem?: boolean}>): Promise<ListResponse<CategoryType>> {
    const res = await $fetch<ListResponse<GetCategoryResponse>>(`api/categories`, {
        query: query
    })

    return {
        items: res.items.map(i => ({
            id: i.id,
            title: i.title,
            icon: i.icon,
            isSystem: i.isSystem ?? false,
            color: i.color
        })),
        total: Number(res.total) 
    } satisfies ListResponse<CategoryType>
}

export async function fetchCategory(categoryId: string): Promise<CategoryType> {
    const res = await $fetch<GetCategoryResponse>(`api/categories/${categoryId}`);

    return {
        id: res.id,
        title: res.title,
        color: res.color,
        icon: res.icon,
        isSystem: res.isSystem ?? false
    };
};

export async function useCreateCategory(request: CreateCategoryRequest): Promise<CreatedRequest> {
    const created = await $fetch<CreatedRequest>(`api/categories`, {
        method: 'POST',
        body: request
    });

    return created
}

export async function useDeleteCategory(categoryId: string): Promise<void> {
    await $fetch(`api/categories/${categoryId}`, {
        method: 'DELETE'
    });
}

export async function useUpdateCategory(categoryId: string, request: UpdateCategoryRequest): Promise<void> {
    await $fetch(`api/categories/${categoryId}`, {
        method: 'PUT',
        body: request
    })
}