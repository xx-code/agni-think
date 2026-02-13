import useApiLink from "~/composables/useApiLink";
import type { ListResponse } from "~/types/api";
import type { GetTagResponse } from "~/types/api/tag";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const query = getQuery(event)
        const res = await $fetch(`${api}/tags`, {
            method: "GET",
            query: query
        });

        const data = (res as ListResponse<GetTagResponse>);

        return data;
    } catch(err) {
        console.log('Get All tags: ' + err);
        return createError({
            status: 500,
            message: 'Get All tags error',
            data: err
        });
    }
});