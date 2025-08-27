import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event)
        const res = await $fetch(`${api}/patrimonies`, {
            method: 'GET',
            query: query
        });

        return res;
    } catch(err) {
        console.log('Get all Patrimony: ' + err);
        return createError({
            status: 500,
            message: 'Get all Patrimony',
            data: err
        });
    }
});