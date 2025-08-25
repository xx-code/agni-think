import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')

        await $fetch(`${api}/patrimony-remove-snapshot/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Snapshot patrimony snapshot: ' + err);
        return createError({
            status: 500,
            message: 'Snapshot Patrimony snapshot',
            data: err
        });
    }
});