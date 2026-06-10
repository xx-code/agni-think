import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/schedule-invoices/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Schedule invoice: ' + err);
        return createError({
            status: 500,
            message: 'Schedule invoices error',
            data: err
        });
    }
});