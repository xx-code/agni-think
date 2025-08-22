import type { ListResponse } from "~/types/api";
import type { GetAllCategoriesResponse } from "~/types/api/category";
import type { CategoryType } from "~/types/ui/category";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useCategories(): UseApiFetchReturn<ListResponse<CategoryType>> {
    const { data, error, refresh } = useFetch('/api/categories', {
        method: 'GET',
        transform: (data: ListResponse<GetAllCategoriesResponse>) => {
            return {
               items: data.items.map(i => ({
                    id: i.categoryId,
                    title: i.title,
                    icon: i.icon,
                    isSystem: i.isSystem,
                    color: i.color
               })),
               totals: data.totals
            } satisfies ListResponse<CategoryType>
        } 
    });

    return { data, error, refresh };
}

// Refactoring
export function useCategoriesNonSys() {
    const { data, error, refresh } = useCategories();

    return computed(() => {
        const categories: CategoryType[] = []; 
        data.value?.items.forEach(cat => {
            if (!cat.isSystem)
                categories.push(cat)
        })

        return categories
    })
}