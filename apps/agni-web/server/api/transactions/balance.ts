import useApiLink from "~/composables/useApiLink";
import type { GetBalanceResponse } from "~/types/api/transaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = readBody(event);

        const res = await $fetch(`${api}/transactions-balance`, {
            method: 'GET',
            query: request 
        });

        const data = (res as GetBalanceResponse);
        return data;
    } catch(err) {
        console.log('Get balance transaction: ' + err);
        return createError({
            status: 500,
            message: 'Balance transaction error',
            data: err
        });
    }
});