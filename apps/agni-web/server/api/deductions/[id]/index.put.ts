import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = await readBody(event);
        await $fetch(`${api}/deductions/${id}`, {
            method: 'PUT',
            body: request
        });
    } catch(err) {
        console.log('Update deduction: ' + err);
        return createError({
            status: 500,
            message: 'Update deuction error',
            data: err
        });
    }
});