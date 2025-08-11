import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/accounts/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete account: ' + err);
        return createError({
            status: 500,
            message: 'Delete Account error',
            data: err
        });
    }
});