import { getRouterParam } from "h3";
import { getApiBase } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const id = getRouterParam(event, "id")
    return await handleRequest(event, `${getApiBase()}/internal-loans/${id}/remove-fund`)
});


