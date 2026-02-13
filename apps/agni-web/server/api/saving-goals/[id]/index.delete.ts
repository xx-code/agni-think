import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/saving-goals/${id}/remove`, {
            body: request, 
            method: 'PUT'
        });
    } catch(err) {
        console.log('Delete budget: ' + err);
        return createError({
            status: 500,
            message: 'Delete budget error',
            data: err
        });
    }
});