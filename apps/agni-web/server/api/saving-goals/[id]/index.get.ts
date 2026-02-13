import useApiLink from "~/composables/useApiLink";
import type { GetSavingGoalResponse } from "~/types/api/saveGoal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/saving-goals/${id}`, {
            method: 'GET'
        });

        const data = (res as GetSavingGoalResponse);

        return data;
    } catch(err) {
        console.log('Get save goal : ' + err);
        return createError({
            status: 500,
            message: 'Get Save goal error',
            data: err
        });
    }
});