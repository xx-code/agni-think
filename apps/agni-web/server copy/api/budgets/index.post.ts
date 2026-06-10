import useApiLink from "~/composables/useApiLink";
import type { CreatedRequest } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        const response = await $fetch(`${api}/budgets`, {
            method: 'POST',
            body: request
        });
        const data = (response as CreatedRequest);

        return data;
    } catch(err) {
        console.log('Create Budget: ' + err);
        return createError({
            status: 500,
            message: 'Create budget error',
            data: err
        });
    }
});