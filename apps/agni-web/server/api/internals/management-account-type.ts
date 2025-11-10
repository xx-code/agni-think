import useApiLink from "~/composables/useApiLink";
import type { GetManagementTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/management-account-type`, {
            method: 'GET'
        });
        const data = (res as GetManagementTypeResponse[]);
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