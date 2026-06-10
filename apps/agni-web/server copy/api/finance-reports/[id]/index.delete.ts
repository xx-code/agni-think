import useApiLink from "~/composables/useApiLink";
import { handleDeleteRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiLink(); 
    const id = getRouterParam(event, "id")
    return await handleDeleteRequest(event, `${api}/finance-reports/${id}`)
});