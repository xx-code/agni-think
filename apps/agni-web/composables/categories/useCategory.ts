import type { GetCategoryResponse } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";

export async function fetchCategory(categoryId: string): Promise<CategoryType> {
    const res = await $fetch<GetCategoryResponse>(`/api/categories/${categoryId}`);

    return {
        id: res.id,
        title: res.title,
        color: res.color,
        icon: res.icon,
        isSystem: res.isSystem ?? false
    };
};