import type { NuxtError } from "nuxt/app";
import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id')
        const query = getQuery(event)

        const res = await $fetch(`${api}/patrimonies/${id}/snapshots`, {
            method: 'GET',
            query: query
        });

        return res
    } catch(err) {
        console.log('Get patrimony snapshot: ' + err);
        return createError({
            status: 500,
            message: 'Get Patrimony snapshot',
            data: err
        });
    }
});