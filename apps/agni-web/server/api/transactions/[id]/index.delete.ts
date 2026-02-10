import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/invoices/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete invoices: ' + err);
        return createError({
            status: 500,
            message: 'Delete invoices error',
            data: err
        });
    }
});