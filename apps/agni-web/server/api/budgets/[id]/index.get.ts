import useApiLink from "~/composables/useApiLink";
import type { GetBudgetResponse } from "~/types/api/budget";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/budgets/${id}`);
        const data = (res as GetBudgetResponse);
        return data;
    } catch(err) {
        console.log('Get budget: ' + err);
        return createError({
            status: 500,
            message: 'Get budget error',
            data: err
        });
    }
});