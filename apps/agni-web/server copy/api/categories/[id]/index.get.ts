import useApiLink from "~/composables/useApiLink";
import type { GetCategoryResponse } from "~/types/api/category";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event,'id');
        const res = await $fetch(`${api}/categories/${id}`, {
            method: 'GET'
        });
        const data = (res as GetCategoryResponse);

        return data;
    } catch(err) {
        console.log('Get category: ' + err);
        return createError({
            status: 500,
            message: 'Get category error',
            data: err
        });
    }
});