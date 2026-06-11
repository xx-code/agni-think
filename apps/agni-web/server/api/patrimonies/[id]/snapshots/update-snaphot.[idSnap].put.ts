import { getApiBase } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const snapshotId = getRouterParam(event, 'idSnap');
    return await handleRequest(event, `${getApiBase()}/patrimonies/update-snapshot/${snapshotId}`);
});
