import useApiLink from "~/composables/useApiLink";
import type { GetContributionTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/contribution-type`, {
            method: 'GET'
        });
        const data = (res as GetContributionTypeResponse[]);
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