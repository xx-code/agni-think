import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, "id")
        const res = await $fetch(`${api}/currencies/${id}`, {
            method: "PUT",
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