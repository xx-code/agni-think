import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAllDeductionTypeResponse } from "~/types/api/deduction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event)
        const res = await $fetch(`${api}/currencies`, {
            method: "GET",
            query: query
        });

        return res;
    } catch(err) {
        console.log('Get all currencies: ' + err);
        return createError({
            status: 500,
            message: 'Get All currencies error',
            data: err
        });
    }
});