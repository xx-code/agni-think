import getApiLink from "~/utils/getApiLink";

export default defineEventHandler(async event => {
    try {
        const api = getApiLink(); 
        const id = getRouterParam(event, 'id');
        await $fetch(`${api}/accounts/${id}`, {
            method: 'DELETE'
        });
    } catch(err) {
        console.log('Delete account: ' + err);
        return createError({
            status: 500,
            message: 'Delete Account error',
            data: err
        });
    }
});