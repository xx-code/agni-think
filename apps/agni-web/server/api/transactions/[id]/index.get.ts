import useApiLink from "~/composables/useApiLink";
import type { GetTransactionResponse } from "~/types/api/transaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/invoices/${id}`, {
            method: 'GET'
        });

        const data = (res as GetTransactionResponse);

        return data;
    } catch(err) {
        console.log('Delete invoices: ' + err);
        return createError({
            status: 500,
            message: 'Delete invoices error',
            data: err
        });
    }
});