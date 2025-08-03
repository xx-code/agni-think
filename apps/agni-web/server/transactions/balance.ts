import { GetBalanceResponse } from "~/types/api/transaction";
import getApiLink from "~/utils/getApiLink";

export default defineEventHandler(async event => {
    try {
        const api = getApiLink(); 
        const request = readBody(event);

        const res = await $fetch(`${api}/accounts/transactions-balance`, {
            method: 'GET',
            query: request 
        });

        const data = (res as GetBalanceResponse);
        return data;
    } catch(err) {
        console.log('Get balance transaction: ' + err);
        throw err;
    }
});