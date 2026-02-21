import type { NuxtError } from "nuxt/app";

export async function handlePostRequest(event: any, url: string) {
   try {
        const request = await readBody(event);
        const res = await $fetch(url, {
            method: 'POST',
            body: request
        });
        return res;
    } catch(err) {
        const nuxtError =  err as NuxtError;
        return createError(nuxtError);
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
        const nuxtError =  err as NuxtError;
        return createError(nuxtError);
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
        const nuxtError =  err as NuxtError;
        return createError(nuxtError);
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
        const nuxtError =  err as NuxtError;
        return createError(nuxtError);
    }
}
