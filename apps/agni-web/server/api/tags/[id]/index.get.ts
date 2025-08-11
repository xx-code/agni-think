import useApiLink from "~/composables/useApiLink";
import type { GetTagResponse } from "~/types/api/tag";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/tags/${id}`, {
            method: 'GET'
        });

        const data = (res as GetTagResponse);

        return data;
    } catch(err) {
        console.log('Delete budget: ' + err);
        return createError({
            status: 500,
            message: 'Delete budget error',
            data: err
        });
    }
});