import useApiLink from "~/composables/useApiLink";
import type { IncreaseSaveGoalRequest } from "~/types/api/saveGoal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request: IncreaseSaveGoalRequest = await readBody(event); 
        await $fetch(`${api}/save-goals/${request.id}/increase-balance`, {
            method: 'PATCH',
            body: request
        });
    } catch(err) {
        console.log('Inscrease save goal: ' + err);
        return createError({
            status: 500,
            message: 'Inscrease save goal error',
            data: err
        });
    }
});