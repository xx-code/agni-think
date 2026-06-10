import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event); 
        await $fetch(`${api}/saving-goals/${request.id}/increase`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Inscrease save goal: ' + err);
        return createError({
            status: 500,
            message: 'Inscrease save goal error',
            data: err
        });
    }
});