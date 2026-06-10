import useApiAgentLink from "~/composables/useApiAgentLink";
import { handlePostRequest } from "~/server/utils";

export default defineEventHandler(async event => { 
    const api = useApiAgentLink(); 
    return await handlePostRequest(event, `${api}/chat`)
})