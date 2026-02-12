import useApiLink from "~/composables/useApiLink";
import type { CreatedRequest } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        const res = await $fetch(`${api}/schedule-invoices`, {
            method: 'POST',
            body: request
        });
        const data = (res as CreatedRequest);

        return data;
    } catch(err) {
        console.log('Create schedule invoice: ' + err);
        return createError({
            status: 500,
            message: 'Create schedule invoice rror',
            data: err
        });
    }
});