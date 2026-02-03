import useApiLink from "~/composables/useApiLink";
import type { CreatedRequest } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        const res = await $fetch(`${api}/deduction-types`, {
            method: 'POST',
            body: request 
        });
        const data = (res as CreatedRequest);
        
        return data;
    } catch(err) {
        console.log('Delete Deduction Types: ' + err);
        return createError({
            status: 500,
            message: 'Delete Deduction Types',
            data: err
        });
    }
});