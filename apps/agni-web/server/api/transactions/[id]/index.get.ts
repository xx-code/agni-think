import useApiLink from "~/composables/useApiLink";
import type { GetTransactionResponse } from "~/types/api/transaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/transactions/${id}`, {
            method: 'GET'
        });

        const data = (res as GetTransactionResponse);

        return data;
    } catch(err) {
        console.log('Delete budget: ' + err);
        return createError({
            status: 500,
            message: 'Delete budget error',
            data: err
        });
    }
});