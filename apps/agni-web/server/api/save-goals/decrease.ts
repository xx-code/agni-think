import useApiLink from "~/composables/useApiLink";
import type { DecreaseSaveGoalRequest } from "~/types/api/saveGoal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request: DecreaseSaveGoalRequest = await readBody(event); 
        await $fetch(`${api}/save-goals/${request.id}/decrease-balance`, {
            method: 'PATCH',
            body: request
        });
    } catch(err) {
        console.log('Descrease savegoal: ' + err);
        return createError({
            status: 500,
            message: 'Descrease savegoal error',
            data: err
        });
    }
});