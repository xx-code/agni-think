import type { Reactive } from "vue";
import type { ListResponse } from "~/types/api";
import type { GetAllSaveGoalResponse, queryFilterSaveGoalRequest } from "~/types/api/saveGoal";
import type { SaveGoalType } from "~/types/ui/saveGoal";
import type { UseApiFetchReturn } from "~/types/utils";

export default function useSaveGoals(query: Reactive<queryFilterSaveGoalRequest>): UseApiFetchReturn<ListResponse<SaveGoalType>> {
    const { data, error, refresh } = useFetch('/api/save-goals', {
        method: 'GET',
        query: query,
        transform: (data: ListResponse<GetAllSaveGoalResponse>) => {
            return {
                items: data.items.map(i => ({
                    id: i.id,
                    title: i.title,
                    description: i.description,
                    balance: i.balance,
                    target: i.target,
                    items: i.items,
                    desirValue: i.desirValue,
                    importance: i.importance,
                    wishDueDate: i.wishDueDate ? new Date(i.wishDueDate) : undefined 
                })),
                totals: data.totals
            } satisfies ListResponse<SaveGoalType> 
        }
    });

    return { data, error, refresh }
}