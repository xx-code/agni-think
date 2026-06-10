import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')

        await $fetch(`${api}/patrimonies/${id}`, {
            method: 'DELETE',
        });
    } catch(err) {
        console.log('Delete Patrimony: ' + err);
        return createError({
            status: 500,
            message: 'Delete Patrimony',
            data: err
        });
    }
});