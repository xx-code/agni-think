import type { H3Error } from "#imports";
import { getQuery, readBody, createError } from "h3";

export async function handleRequest(event: any, urlPath: string) {
    try {
        const query = getQuery(event);
        const hasBody = ['POST', 'PUT', 'PATCH'].includes(event.method);
        const body = hasBody ? await readBody(event) : undefined;
        const res = await $fetch(urlPath, {
            method: event.method,
            query,
            ...(hasBody && { body })
        });
        return res;
    } catch (err) {
        throw createError(err as H3Error);
    }
}