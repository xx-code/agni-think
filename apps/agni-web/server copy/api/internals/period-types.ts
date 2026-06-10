import useApiLink from "~/composables/useApiLink";
import type { GetPeriodTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/period-type`, {
            method: 'GET'
        });
        const data = (res as GetPeriodTypeResponse[]);
        return data;
    } catch(err) {
        console.log('Get period type: ' + err);
        return createError({
            status: 500,
            message: 'Get period type error',
            data: err
        });
    }
});