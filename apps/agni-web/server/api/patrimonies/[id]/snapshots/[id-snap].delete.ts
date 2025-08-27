import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id-snap')

        await $fetch(`${api}/patrimony-remove-snapshot/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Snapshot patrimony snapshot: ' + err);
        return createError({
            status: 499,
            message: 'Snapshot Patrimony snapshot',
            data: err
        });
    }
});