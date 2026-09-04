import type { DeletedResponse } from "~/types/api";
import type { Deleted } from "~/types/ui";

export function deletedResponseToDeleted(data: DeletedResponse): Deleted {
    return data
}