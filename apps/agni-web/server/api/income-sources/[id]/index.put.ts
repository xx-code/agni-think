import useApiLink from "~/composables/useApiLink"
import { handlePutRequest } from "~/server/utils"

export default defineEventHandler(async event => {
    const link = useApiLink()
    const id = getRouterParam(event, 'id')
    return await handlePutRequest(event, `${link}/income-sources/${id}` )
})