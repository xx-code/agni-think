import type { GetTagResponse } from "../api/tag"

export type EditTagType = {
    value: string
    color: string
}

export type TagType = Omit<GetTagResponse, 'system' | 'archived'> & {
    isSystem: boolean
    isArchived: boolean
}