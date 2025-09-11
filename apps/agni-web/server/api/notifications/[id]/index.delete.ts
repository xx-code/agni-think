import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')

        const res = await $fetch(`${api}/notifications/${id}`, {
            method: 'DELETE'
        });
        
        return res
    } catch(err) {
        return createError({
            status: 500,
            message: 'Notifcaiton Error',
            data: err
        });
    }
});