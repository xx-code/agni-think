import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/transactions/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete transaction: ' + err);
        return createError({
            status: 500,
            message: 'Delete transaction error',
            data: err
        });
    }
});