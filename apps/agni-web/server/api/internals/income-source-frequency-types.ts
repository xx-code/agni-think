import useApiLink from "~/composables/useApiLink";
import { handleGetRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiLink(); 
    return handleGetRequest(event, `${api}/internal/income-source-frequency-type`) 
});