import useApiLink from "~/composables/useApiLink";
import type { GetSpendAnalysticResponse } from "~/types/api/analytics";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/spends`, {
            method: 'GET',
            query: query
        });
        const data = (res as GetSpendAnalysticResponse);
        return data;
    } catch(err) {
        console.log('Get estimation amount: ' + err);
        return createError({
            status: 500,
            message: 'Get estimation amount',
            data: err
        });
    }
});