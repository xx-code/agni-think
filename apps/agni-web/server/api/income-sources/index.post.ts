import useApiLink from "~/composables/useApiLink";
import { handlePostRequest } from "~/server/utils";

export default defineEventHandler(async event => {
    const link = useApiLink()
    return await handlePostRequest(event, `${link}/income-sources`)
})