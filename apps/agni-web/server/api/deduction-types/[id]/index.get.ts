import useApiLink from "~/composables/useApiLink";
import type { GetDeductionTypeResponse } from "~/types/api/deduction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event,'id');
        const res = await $fetch(`${api}/deductions/${id}`, {
            method: 'GET'
        });
        const data = (res as GetDeductionTypeResponse);

        return data;
    } catch(err) {
        console.log('Get deductions: ' + err);
        return createError({
            status: 500,
            message: 'Get deduction error',
            data: err
        });
    }
});