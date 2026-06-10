import useApiLink from "~/composables/useApiLink";
import type { CreatedRequest } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        const res = await $fetch(`${api}/accounts`, {
            method: 'POST',
            body: request
        });
        
        const data = (res as CreatedRequest);

        return data;
    } catch(err) {
        console.log('Post account: ' + err);
        return createError({
            status: 500,
            message: 'Post Account error',
            data: err
        });
    }
});