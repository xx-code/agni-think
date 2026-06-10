import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const body = await readBody(event)
        const res = await $fetch(`${api}/currencies`, {
            method: "POST",
            query: body
        });

        return res;
    } catch(err) {
        console.log('Get all currencies: ' + err);
        return createError({
            status: 500,
            message: 'Get All currencies error',
            data: err
        });
    }
});