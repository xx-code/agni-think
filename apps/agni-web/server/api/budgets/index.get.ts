import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetAllBudgetResponse } from "~/types/api/budget";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/budgets`, {
            method: 'GET'
        });
        const data = (res as ListResponse<GetAllBudgetResponse>);
        return data;
    } catch(err) {
        console.log('Get all budget: ' + err);
        return createError({
            status: 500,
            message: 'Get all budget error',
            data: err
        });
    }
});