import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetDeductionResponse } from "~/types/api/deduction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event)
        const res = await $fetch(`${api}/deductions`, {
            method: "GET",
            query: query
        });
        const data = (res as ListResponse<GetDeductionResponse>);
        return data;
    } catch(err) {
        console.log('Get all deductions: ' + err);
        return createError({
            status: 500,
            message: 'Get All deductions error',
            data: err
        });
    }
});