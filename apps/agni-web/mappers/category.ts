import type { ListResponse } from "~/types/api";
import type { GetCategoryResponse } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";

export function categoryResponseToCategory(data: GetCategoryResponse): CategoryType {
    return {
        id: data.id,
        title: data.title,
        icon: data.icon,
        color: data.color,
        isSystem: data.isSystem ?? false
    }
}

export function listCategoriesResponseToListCategories(data: ListResponse<GetCategoryResponse>): ListResponse<CategoryType> {
    return {
        items: data.items.map(i => categoryResponseToCategory(i)),
        total: Number(data.total)
    }
}
