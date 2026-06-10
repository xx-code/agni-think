import useApiLink from "~/composables/useApiLink";
import type { GetIntensityDesirTypeResponse } from "~/types/api/internal";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const res = await $fetch(`${api}/internal/intensity-desir-type`, {
            method: 'GET'
        });
        const data = (res as GetIntensityDesirTypeResponse[]);
        return data;
    } catch(err) {
        console.log('Internal intensity desir Type: ' + err);
        return createError({
            status: 500,
            message: 'Internal intensity desir Type error',
            data: err
        });
    }
});