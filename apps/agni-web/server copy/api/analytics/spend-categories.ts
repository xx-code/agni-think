import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/spend-categories`, {
            method: 'GET',
            query: query
        });
        return res;
    } catch(err) {
        console.log('Get category estimation amount: ' + err);
        return createError({
            status: 500,
            message: 'Get category estimation amount',
            data: err
        });
    }
});