import { GetAccountTypeResponse } from "~/types/api/internal";
import getApiLink from "~/utils/getApiLink";

export default defineEventHandler(async event => {
    try {
        const api = getApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/accounts/${id}`, {
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