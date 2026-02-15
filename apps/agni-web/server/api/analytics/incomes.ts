import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/incomes`, {
            method: 'GET',
            query: query
        });
        return res;
    } catch(err) {
        console.log('Get estimation amount: ' + err);
        return createError({
            status: 500,
            message: 'Get estimation amount',
            data: err
        });
    }
});