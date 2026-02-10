import type { Reactive } from "vue";
import type { ListResponse, QueryFilterRequest } from "~/types/api";
import type { GetCategoryResponse } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useCategories(query: Reactive<QueryFilterRequest>): UseApiFetchReturn<ListResponse<CategoryType>> {
    const { data, error, refresh } = useFetch('/api/categories', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetCategoryResponse>) => {
            return {
               items: data.items.map(i => ({
                    id: i.id,
                    title: i.title,
                    icon: i.icon,
                    isSystem: i.isSystem ?? false,
                    color: i.color
               })),
               totals: Number(data.totals) 
            } satisfies ListResponse<CategoryType>
        } 
    });

    return { data, error, refresh };
}

// Refactoring
export function useCategoriesNonSys(query: Reactive<QueryFilterRequest>) {
    const { data, error, refresh } = useCategories(query);

    return computed(() => {
        const categories: CategoryType[] = []; 
        data.value?.items.forEach(cat => {
            if (!cat.isSystem)
                categories.push(cat)
        })

        return categories
    })
}

export async function fetchCategories(query: Reactive<QueryFilterRequest>): Promise<ListResponse<CategoryType>> {
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
        totals: Number(res.totals) 
    } satisfies ListResponse<CategoryType>
}