import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')

        const request = await readBody(event)
        const res = await $fetch(`${api}/patrimonies/${id}/add-snapshot`, {
            method: 'POST',
            body: request 
        });
        
        return res
    } catch(err) {
        console.log('Add patrimony snapshot: ' + err);
        return createError({
            status: 500,
            message: 'Add Patrimony snapshot',
            data: err
        });
    }
});