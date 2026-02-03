import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/deduction-types/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete deduction types: ' + err);
        return createError({
            status: 500,
            message: 'Delete deduction types error',
            data: err
        });
    }
});