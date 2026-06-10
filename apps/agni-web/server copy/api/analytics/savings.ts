import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/savings`, {
            method: 'GET',
            query: query
        });
        return res;
    } catch(err) {
        console.log('Get saving estimation amount: ' + err);
        return createError({
            status: 500,
            message: 'Get saving estimation amount',
            data: err
        });
    }
});