import useApiLink from "~/composables/useApiLink";
import type { GetImportanceGoalTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/importance-type`, {
            method: 'GET'
        });
        const data = (res as GetImportanceGoalTypeResponse[]);
        return data;
    } catch(err) {
        console.log('Internal importance Type: ' + err);
        return createError({
            status: 500,
            message: 'Internal importance Type error',
            data: err
        });
    }
});