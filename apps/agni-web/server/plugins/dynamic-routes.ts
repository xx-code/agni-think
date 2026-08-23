import { withQuery } from 'ufo';
import { getApiBase, getApiAgent } from '~/utils/env';
import { API_ROUTES } from '~/shared/routes';
import type { ApiRouteDefinition } from '~/types/shared/routes';

export default defineNitroPlugin((nitroApp) => {
    const mainApiBase = getApiBase()
    const agentApiBase = getApiAgent()

    const routes: ApiRouteDefinition[] = Object.values(API_ROUTES).flatMap(group => Object.values(group))

    routes.forEach((config) => {
        nitroApp.router.use(
            config.serverPath,
            defineEventHandler(async (event) => {
                try {
                    const apiBase = config.backend === 'agent' ? agentApiBase : mainApiBase
                    let targetPath = config.apiPath

                    const params = getRouterParams(event)
                    const query = getQuery(event)

                    Object.keys(params).forEach((paramKey) => {
                        targetPath = targetPath.replace(`:${paramKey}`, params[`${paramKey}`] as string)
                    })

                    const targetUrl = withQuery(`${apiBase}${targetPath}`, query)

                    return await proxyRequest(event, targetUrl);
                } catch (err: any) {
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