import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/cashflow`, {
            method: 'GET',
            query: query
        });
        return res;
    } catch(err) {
        console.log('Get Cashflow: ' + err);
        return createError({
            status: 500,
            message: 'Get Cashflow',
            data: err
        });
    }
});