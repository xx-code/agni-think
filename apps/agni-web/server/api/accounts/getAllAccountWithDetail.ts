import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAccountWithDetailResponse } from "~/types/api/account";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);
        const res = await $fetch(`${api}/accounts/with-detail`, {
            method: 'GET',
            query: query
        });
        const data = (res as ListResponse<GetAccountWithDetailResponse>);

        return data;
    } catch(err) {
        console.log('Get all account detail:' + err);
        return createError({
            status: 500,
            message: 'Get All Account error detail',
            data: err
        });
    }
});