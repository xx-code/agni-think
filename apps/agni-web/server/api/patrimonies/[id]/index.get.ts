import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')
        const query = getQuery(event)

        const res = await $fetch(`${api}/patrimonies/${id}`, {
            method: 'GET',
            query: query 
        });

        return res
    } catch(err) {
        console.log('Get Patrimony: ' + err);
        return createError({
            status: 500,
            message: 'Get Patrimony',
            data: err
        });
    }
});