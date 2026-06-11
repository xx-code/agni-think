import { getApiBase } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const id = getRouterParam(event, 'id-snap');
    return await handleRequest(event, `${getApiBase()}/patrimonies/remove-snapshot/${id}`);
});
