import useApiLink from "~/composables/useApiLink";
import type { CreatedRequest } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        const res = await $fetch(`${api}/schedule-transactions`, {
            method: 'POST',
            body: request
        });
        const data = (res as CreatedRequest);

        return data;
    } catch(err) {
        console.log('Create schedule transaction: ' + err);
        return createError({
            status: 500,
            message: 'Create schedule transaction rror',
            data: err
        });
    }
});