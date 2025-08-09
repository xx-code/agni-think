import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/accounts/${id}`, {
            method: 'PUT',
            body: request 
        });

    } catch(err) {
        console.log('Put account: ' + err);
        return createError({
            status: 500,
            message: 'Put Account error',
            data: err
        });
    }
});