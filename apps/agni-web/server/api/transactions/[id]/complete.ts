import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/invoices/${id}/completed`, {
            method: 'PUT',
        });
    } catch(err) {
        console.log('Complete transaction: ' + err);
        return createError({
            status: 500,
            message: 'Complete transaction error',
            data: err
        });
    }
});