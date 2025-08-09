import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        await $fetch(`${api}/freeze-transaction`, {
            method: 'POST',
            body: request
        });
    } catch(err) {
        console.log('Freeze transaction: ' + err);
        return createError({
            status: 500,
            message: 'Freeze transaction error',
            data: err
        });
    }
});