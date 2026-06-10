import useApiLink from "~/composables/useApiLink";
import type { GetTransactionTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/transaction-type`, {
            method: 'GET'
        });
        const data = (res as GetTransactionTypeResponse[]);
        return data; 
    } catch(err) {
        console.log('Get transaction type: ' + err);
        return createError({
            status: 500,
            message: 'Get transaction type error',
            data: err
        });
    }
});