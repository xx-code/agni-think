import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/analytics/spend-tags`, {
            method: 'GET',
            query: query
        });
        return res;
    } catch(err) {
        console.log('Get estim tag: ' + err);
        return createError({
            status: 500,
            message: 'Get estim tag',
            data: err
        });
    }
});