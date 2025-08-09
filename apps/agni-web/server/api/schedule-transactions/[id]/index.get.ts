import useApiLink from "~/composables/useApiLink";
import type { GetScheduleTransactionResponse } from "~/types/api/scheduleTransaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/schedule-transactions/${id}`, {
            method: 'GET'
        });

        const data = (res as GetScheduleTransactionResponse);

        return data;
    } catch(err) {
        console.log('Delete budget: ' + err);
        return createError({
            status: 500,
            message: 'Delete budget error',
            data: err
        });
    }
});