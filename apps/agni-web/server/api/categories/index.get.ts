import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetCategoryResponse } from "~/types/api/category";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event)
        const res = await $fetch(`${api}/categories`, {
            method: "GET",
            query: query
        });
        const data = (res as ListResponse<GetCategoryResponse>);
        return data;
    } catch(err) {
        console.log('Get all Categories: ' + err);
        return createError({
            status: 500,
            message: 'Get All Categories error',
            data: err
        });
    }
});