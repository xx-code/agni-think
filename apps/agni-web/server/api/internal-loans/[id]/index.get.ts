import useApiLink from "~/composables/useApiLink";
import { handleGetRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiLink(); 
    const id = getRouterParam(event, "id")
    return await handleGetRequest(event, `${api}/internal-loans/${id}`)
});