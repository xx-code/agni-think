import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/tags/${id}`);
    } catch(err) {
        console.log('Delete tag: ' + err);
        return createError({
            status: 500,
            message: 'Delete tag error',
            data: err
        });
    }
});