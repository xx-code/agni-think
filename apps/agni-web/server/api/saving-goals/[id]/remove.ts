import { getApiBase } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const id = getRouterParam(event, 'id');
    return await handleRequest(event, `${getApiBase()}/saving-goals/${id}/remove`);
});
