import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { FilterTransactionQuery, GetTransactionResponse } from "~/types/api/transaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = await readBody(event);

        const response = await $fetch(`${api}/transactions`, {
            query: query
        });

        const data = (response as ListResponse<GetTransactionResponse>);

        return data;
    } catch(err) {
        console.log('Pagination transaction: ' + err);
        return createError({
            status: 500,
            message: 'Pagination transaction error',
            data: err
        });
    }
});