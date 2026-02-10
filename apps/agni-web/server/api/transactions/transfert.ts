import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        await $fetch(`${api}/invoices/transfert`, {
            method: 'POST',
            body: request
        });
    } catch(err) {
        console.log('tranfert transaction: ' + err);
        return createError({
            status: 500,
            message: 'transfert transaction error',
            data: err
        });
    }
});