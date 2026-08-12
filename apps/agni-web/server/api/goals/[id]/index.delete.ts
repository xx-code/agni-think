import { handleRequest } from "~/server/utils";
import { getApiBase } from "~/utils/env";

export default defineEventHandler(async event => {
    const id = getRouterParam(event, 'id');
    return await handleRequest(event, `${getApiBase()}/goals/${id}`);
});


