import { withQuery } from 'ufo';
import { getApiBase } from '~/utils/env';
import { API_ROUTES } from '~/shared/routes';
import type { ApiRouteDefinition } from '~/types/shared/routes';

const TOKEN_COOKIE = 'access_token'
const COOKIE_OPTIONS = {
    httpOnly: true,
    secure: !import.meta.dev,
    sameSite: 'lax' as const,
    path:'/'
}

export default defineNitroPlugin((nitroApp) => {
    const mainApiBase = getApiBase()
    const routes: ApiRouteDefinition[] = Object.values(API_ROUTES).flatMap(group => Object.values(group))

    routes.forEach((config) => {
        nitroApp.router.use(
            config.serverPath,
            defineEventHandler(async (event) => {
                try {
                    const apiBase = mainApiBase
                    let targetPath = config.apiPath

                    const params = getRouterParams(event)
                    const query = getQuery(event)

                    Object.keys(params).forEach((paramKey) => {
                        targetPath = targetPath.replace(`:${paramKey}`, params[`${paramKey}`] as string)
                    })

                    const targetUrl = withQuery(`${apiBase}${targetPath}`, query)
                    const token = getCookie(event, TOKEN_COOKIE)

                    if (config.session === 'create-oidc') {
                        const body = await readBody(event)
                        await $fetch.raw(targetUrl, {
                            method: 'POST',
                            headers: { authorization: `Bearer ${body.token}` }
                        })
                        setCookie(event, TOKEN_COOKIE, body.token, {
                            ...COOKIE_OPTIONS,
                            maxAge: 60 * 60, // same as backend
                        })
                        return { ok: true }
                    }

                    if (config.session === 'create') {
                        const res = await $fetch.raw<{ token: string }>(targetUrl, {
                            method: 'POST',
                            body: await readRawBody(event),
                            headers: { 'content-type': getHeader(event, 'content-type') ?? '' },
                        })
                        setCookie(event, TOKEN_COOKIE, res._data!.token, {
                            ...COOKIE_OPTIONS,
                            maxAge: 60 * 60, // same as backend
                        })
                        return { ok: true }
                    }

                    if (config.session === 'destroy') {
                        try {
                            await $fetch(targetUrl, {
                                method: 'GET',
                                headers: token ? { authorization: `Bearer ${token}` } : {},
                            })
                        } finally {
                            deleteCookie(event, TOKEN_COOKIE, COOKIE_OPTIONS)
                        }
                        return sendNoContent(event)
                    }

                    const result = await proxyRequest(event, targetUrl, {
                        headers: !config.public && token
                            ? { authorization: `Bearer ${token}` }
                            : undefined,
                    })

                    if (getResponseStatus(event) === 401) {
                        deleteCookie(event, TOKEN_COOKIE, COOKIE_OPTIONS)
                    }
                    return result 
                } catch (err: any) {
                    if (err?.statusCode === 401)
                        deleteCookie(event, TOKEN_COOKIE, COOKIE_OPTIONS)

                    throw createError({
                        statusCode: err?.statusCode || 500,
                        statusMessage: err?.statusMessage || "Backend Request Failed",
                        data: err?.data
                    });
                }
            }),
            config.method.toLowerCase() as any
        )
    })
})