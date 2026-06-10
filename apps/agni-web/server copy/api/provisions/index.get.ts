import useApiLink from "~/composables/useApiLink"
import { handleGetRequest } from "~/server/utils"

export default defineEventHandler(async event => {
    const link = useApiLink()
    return await handleGetRequest(event, `${link}/provisions`)
})