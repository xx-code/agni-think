import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/budget-rules`, {
            method: 'GET',
            query: query
        });
        return res;
    } catch(err) {
        console.log('Get budget rules: ' + err);
        return createError({
            status: 500,
            message: 'Get budget rules',
            data: err
        });
    }
});