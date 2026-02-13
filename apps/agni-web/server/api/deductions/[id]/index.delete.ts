import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/deductions/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete deduction: ' + err);
        return createError({
            status: 500,
            message: 'Delete deduction error',
            data: err
        });
    }
});