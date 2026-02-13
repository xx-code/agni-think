import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/invoices/${id}`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Put invoices: ' + err);
        return createError({
            status: 500,
            message: 'Put invoices error',
            data: err
        });
    }
});