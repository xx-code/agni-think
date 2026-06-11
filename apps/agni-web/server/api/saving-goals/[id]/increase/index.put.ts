import { getApiBase } from "~/utils/env";
import { readBody } from "h3";

export default defineEventHandler(async event => {
    const request = await readBody(event);
    return await $fetch(`${getApiBase()}/saving-goals/${request.id}/increase`, {
        method: 'PUT',
        body: request
    });
});
