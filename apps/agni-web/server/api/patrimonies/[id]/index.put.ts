import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')
        const request = await readBody(event)

        await $fetch(`${api}/patrimonies/${id}`, {
            method: 'PUT',
            body: request
        });

    } catch(err) {
        console.log('Put Patrimony: ' + err);
        return createError({
            status: 500,
            message: 'Put Patrimony',
            data: err
        });
    }
});