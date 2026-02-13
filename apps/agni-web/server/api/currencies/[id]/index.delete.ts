import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, "id")
        const res = await $fetch(`${api}/currencies/${id}`, {
            method: "DELETE",
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