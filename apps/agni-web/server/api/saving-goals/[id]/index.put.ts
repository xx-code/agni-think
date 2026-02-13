import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event); 
        await $fetch(`${api}/saving-goals/${id}`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Update save goal: ' + err);
        return createError({
            status: 500,
            message: 'Update save goal error',
            data: err
        });
    }
});