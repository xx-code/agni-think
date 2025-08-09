import useApiLink from "~/composables/useApiLink";
import type { GetAccountResponse } from "~/types/api/account";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/accounts/${id}`, {
            method: 'GET'
        });
        const data = (res as GetAccountResponse);
        return data;
    } catch(err) {
        console.log('Get account: ' + err);
        return createError({
            status: 500,
            message: 'Get Account error',
            data: err
        });
    }
});