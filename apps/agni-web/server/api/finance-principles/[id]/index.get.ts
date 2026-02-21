import useApiLink from "~/composables/useApiLink"
import { handleGetRequest } from "~/server/utils"

export default defineEventHandler(async event => {
    const link = useApiLink()
    const id = getRouterParam(event, 'id')
    return await handleGetRequest(event, `${link}/finance-principles/${id}`)
})