import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, "id")
        const res = await $fetch(`${api}/currencies/${id}`, {
            method: "GET",
        });

        return res;
    } catch(err) {
        console.log('Get currencies: ' + err);
        return createError({
            status: 500,
            message: 'Get currencies error',
            data: err
        });
    }
});