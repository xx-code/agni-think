import { getApiBase } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    return handleRequest(event, `${getApiBase()}/internals/income-source-frequency-type`)
});
