import useApiLink from "~/composables/useApiLink";
import { handleGetRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiLink(); 
    return await handleGetRequest(event, `${api}/analytics/annual-outlook`)
});