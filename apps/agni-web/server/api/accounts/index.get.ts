import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAllAccountResponse } from "~/types/api/account";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/accounts`, {
            method: 'GET'
        });
        const data = (res as ListResponse<GetAllAccountResponse>);

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