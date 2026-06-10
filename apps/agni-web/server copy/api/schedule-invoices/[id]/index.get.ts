import useApiLink from "~/composables/useApiLink";
import type { GetScheduleInvoiceResponse } from "~/types/api/scheduleTransaction";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const id = getRouterParam(event, 'id');
        const res = await $fetch(`${api}/schedule-invoices/${id}`, {
            method: 'GET'
        });

        const data = (res as GetScheduleInvoiceResponse);

        return data;
    } catch(err) {
        console.log('Schedule Invoice: ' + err);
        return createError({
            status: 500,
            message: 'Schedule Invoice error',
            data: err
        });
    }
});