import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/transactions/${id}`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Put transaction: ' + err);
        return createError({
            status: 500,
            message: 'Put transaction error',
            data: err
        });
    }
});