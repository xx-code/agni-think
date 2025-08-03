import getApiLink from "~/utils/getApiLink";

export default defineEventHandler(async event => {
    try {
        const api = getApiLink(); 
        const id = getRouterParam(event, 'id');
        const request = readBody(event);
        await $fetch(`${api}/accounts/${id}`, {
            method: 'PUT',
            body: request 
        });

    } catch(err) {
        console.log('Put account: ' + err);
        return createError({
            status: 500,
            message: 'Put Account error',
            data: err
        });
    }
});