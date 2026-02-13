import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        await $fetch(`${api}/invoices/create-freeze`, {
            method: 'POST',
            body: request
        });
    } catch(err) {
        console.log('Freeze invoice: ' + err);
        return createError({
            status: 500,
            message: 'Freeze invoice error',
            data: err
        });
    }
});