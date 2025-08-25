import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/patrimonies`, {
            method: 'GET'
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