import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/tags/${id}`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Update tag: ' + err);
        return createError({
            status: 500,
            message: 'Update tag error',
            data: err
        });
    }
});