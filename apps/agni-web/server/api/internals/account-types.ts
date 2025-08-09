import useApiLink from "~/composables/useApiLink";
import type { GetAccountTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/account-type`, {
            method: 'GET'
        });
        const data = (res as GetAccountTypeResponse[]);
        return data;
    } catch(err) {
        console.log('Internal Account Type: ' + err);
        return createError({
            status: 500,
            message: 'Internal Account Type error',
            data: err
        });
    }
});