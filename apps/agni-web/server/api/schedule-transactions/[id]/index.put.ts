import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/schedule-transactions/${id}`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Updatge budget: ' + err);
        return createError({
            status: 500,
            message: 'Udpate budget error',
            data: err
        });
    }
});