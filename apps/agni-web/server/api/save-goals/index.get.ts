import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAllSaveGoalResponse } from "~/types/api/saveGoal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const response = await $fetch(`${api}/save-goals`, {
            method: 'GET'
        });

        const data = (response as ListResponse<GetAllSaveGoalResponse>);

        return data;
    } catch(err) {
        console.log('Get all Save Goal: ' + err);
        return createError({
            status: 500,
            message: 'Get all Save goal error',
            data: err
        });
    }
});