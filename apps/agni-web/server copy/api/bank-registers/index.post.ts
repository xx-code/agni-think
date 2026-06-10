import useApiLink from "~/composables/useApiLink";
import { handlePostRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiLink(); 
    return await handlePostRequest(event, `${api}/bank-registers`)
});