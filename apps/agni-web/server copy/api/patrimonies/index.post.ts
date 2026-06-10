import useApiLink from "~/composables/useApiLink";

export default defineEventHandler(async event => {
    try {
        const api = useApiLink(); 
        const request = await readBody(event)
        const res = await $fetch(`${api}/patrimonies`, {
            method: 'POST',
            body: request
        });

        return res;
    } catch(err) {
        console.log('Post Patrimony: ' + err);
        return createError({
            status: 500,
            message: 'Post Patrimony',
            data: err
        });
    }
});