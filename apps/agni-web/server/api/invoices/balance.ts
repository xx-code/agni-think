import useApiLink from "~/composables/useApiLink";
import type { GetBalanceResponse } from "~/types/api/transaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);

        const res = await $fetch(`${api}/invoices/balances`, {
            method: 'GET',
            query: query 
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