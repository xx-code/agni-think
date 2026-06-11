import { getApiBase } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    return await handleRequest(event, `${getApiBase()}/analytics/annual-outlook`)
});
