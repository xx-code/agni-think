import { getRouterParam } from "h3";
import { getApiAgent } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const id = getRouterParam(event, "id")
    return await handleRequest(event, `${getApiAgent()}/agent-suggestions/${id}/confirm`)
});
