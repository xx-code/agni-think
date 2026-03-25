import type { H3Error } from "#imports";
export async function handlePostRequest(event: any, url: string) {
   try {
        const request = await readBody(event);
        const res = await $fetch(url, {
            method: 'POST',
            body: request
        });
        return res;
    } catch(err) {
        throw createError(err as H3Error);
    }
}

export async function handleGetRequest(event:any, url: string) {
    try {
        const query = getQuery(event)
        const res = await $fetch(url, {
            method: 'GET',
            query: query 
        });
        return res;
    } catch(err) {
        throw createError(err as H3Error);
    }
} 

export async function handleDeleteRequest(event:any, url: string) {
    try {
        const query = getQuery(event)
        const res = await $fetch(url, {
            method: 'DELETE',
            query: query 
        });
        return res;
    } catch(err) {
        throw createError(err as H3Error);
    }
}

export async function handlePutRequest(event:any, url: string) {
    try {
        const query = getQuery(event)
        const request = await readBody(event);
        const res = await $fetch(url, {
            method: 'PUT',
            body: request,
            query: query 
        });
        return res;
    } catch(err) {
        throw createError(err as H3Error);
    }
}
