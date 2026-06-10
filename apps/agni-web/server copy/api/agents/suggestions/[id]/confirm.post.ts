import useApiAgentLink from "~/composables/useApiAgentLink";
import { handlePostRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const api = useApiAgentLink(); 
    const id = getRouterParam(event, "id")
    return await handlePostRequest(event, `${api}/agent-suggestions/${id}/confirm`)
});