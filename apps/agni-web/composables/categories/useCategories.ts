import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetCategoryResponse } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";

export async function fetchCategories(query: Reactive<QueryFilterRequest & { isSystem?: boolean}>): Promise<ListResponse<CategoryType>> {
    const res = await $fetch<ListResponse<GetCategoryResponse>>('/api/categories', {
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