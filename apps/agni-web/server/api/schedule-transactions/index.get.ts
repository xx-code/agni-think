import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAllScheduleTransactionsResponse } from "~/types/api/scheduleTransaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event)
        const res = await $fetch(`${api}/schedule-transactions`, {
            method: 'GET',
            query: query
        });

        const data = (res as ListResponse<GetAllScheduleTransactionsResponse>);

        return data;
    } catch(err) {
        console.log('Create schedule transaction: ' + err);
        return createError({
            status: 500,
            message: 'Create schedule transaction error',
            data: err
        });
    }
});