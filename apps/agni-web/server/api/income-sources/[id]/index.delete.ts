import useApiLink from "~/composables/useApiLink"
import { handleDeleteRequest } from "~/server/utils"

export default defineEventHandler(async event => {
    const link = useApiLink()
    const id = getRouterParam(event, 'id')
    return await handleDeleteRequest(event, `${link}/income-sources/${id}`)
})