import type { GetCategoryResponse } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useCategory(categoryId: string): UseApiFetchReturn<CategoryType> {
    const { data, error, refresh } = useFetch(`/api/categories/${categoryId}`, {
        method: "GET",
        transform: (data: GetCategoryResponse) => {
            return {
                id: data.categoryId,
                title: data.title,
                color: data.color,
                icon: data.icon,
                isSystem: data.isSystem
            } satisfies CategoryType
        } 
    });

    return { data, error, refresh };
}

export async function fetchCategory(categoryId: string): Promise<CategoryType> {
    const res = await $fetch<GetCategoryResponse>(`/api/categories/${categoryId}`);

    return {
        id: res.categoryId,
        title: res.title,
        color: res.color,
        icon: res.icon,
        isSystem: res.isSystem
    };
};