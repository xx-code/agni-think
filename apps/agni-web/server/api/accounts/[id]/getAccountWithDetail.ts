import useApiLink from "~/composables/useApiLink";
import type { GetAccountWithDetailResponse } from "~/types/api/account";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/accounts-with-detail/${id}`, {
            method: 'GET'
        });
        const data = (res as GetAccountWithDetailResponse);
        return data;
    } catch(err) {
        console.log('Get account detail: ' + err);
        return createError({
            status: 500,
            message: 'Get Account error',
            data: err
        });
    }
});