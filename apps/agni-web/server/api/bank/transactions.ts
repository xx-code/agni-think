import { getApiAgent } from "~/utils/env";
import { handleRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    return await handleRequest(event, `${getApiAgent()}/bank-transaction`)
});
