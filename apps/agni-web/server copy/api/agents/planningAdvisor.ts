import type { NuxtError } from "nuxt/app";
import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event);
        const res = await $fetch(`${api}/analytics/save-goal-planning`, {
            method: 'POST',
            body: request
        });

        return res;
    } catch(err) {
        const nuxtError =  err as NuxtError;
        return createError(nuxtError);
    }
})