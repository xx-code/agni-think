import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAllAccountResponse, GetAllAccountWithPastBalanceResponse } from "~/types/api/account";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/accounts-with-past-balance`, {
            method: 'GET',
            query: query
        });
        const data = (res as ListResponse<GetAllAccountWithPastBalanceResponse>);

        return data;
    } catch(err) {
        console.log('Get all account:' + err);
        return createError({
            status: 500,
            message: 'Get All Account error',
            data: err
        });
    }
});