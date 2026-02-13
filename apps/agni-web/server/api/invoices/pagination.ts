import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type {  GetInvoiceResponse } from "~/types/api/transaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event);

        const response = await $fetch(`${api}/invoices`, {
            query: query
        });

        const data = (response as ListResponse<GetInvoiceResponse>);

        return data;
    } catch(err) {
        console.log('Pagination invoices: ' + err);
        return createError({
            status: 500,
            message: 'Pagination invoices error',
            data: err
        });
    }
});