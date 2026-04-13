import useApiAgentLink from "~/composables/useApiAgentLink";
import { handleGetRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiAgentLink(); 
    return await handleGetRequest(event, `${api}/init-external-transactions`)
});