import useApiLink from "~/composables/useApiLink";
import { handlePutRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiLink(); 
    const id = getRouterParam(event, "id")
    return await handlePutRequest(event, `${api}/bank-registers/${id}`)
});