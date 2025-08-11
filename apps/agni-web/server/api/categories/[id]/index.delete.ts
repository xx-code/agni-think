import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/categories/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete category: ' + err);
        return createError({
            status: 500,
            message: 'Delete category error',
            data: err
        });
    }
});