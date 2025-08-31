import useApiLink from "~/composables/useApiLink";
import type { GetEstimationLeftAmountResponse } from "~/types/api/analytics";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/cashflow`, {
            method: 'GET',
            query: query
        });
        const data = (res as GetEstimationLeftAmountResponse);
        return data;
    } catch(err) {
        console.log('Get Cashflow: ' + err);
        return createError({
            status: 500,
            message: 'Get Cashflow',
            data: err
        });
    }
});