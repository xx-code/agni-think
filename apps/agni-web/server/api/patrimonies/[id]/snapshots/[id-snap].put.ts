import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')
        const snapshotId = getRouterParam(event, 'id-snap')

        const request = await readBody(event)
        await $fetch(`${api}/patrimonies/${id}/update-snapshot/${snapshotId}`, {
            method: 'PUT',
            body: request 
        });
    } catch(err) {
        console.log('Update patrimony snapshot: ' + err);
        return createError({
            status: 500,
            message: 'Update Patrimony snapshot',
            data: err
        });
    }
});