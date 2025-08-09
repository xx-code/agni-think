export type GetTagResponse = {
    id: string
    value: string
    color?: string
    isSystem: boolean
}

export type GetAllTagsResponse = {
    id: string
    value: string
    color?: string
    isSystem: boolean
}

export type CreateTagRequest = {
    value: string
    color: string
}

export type UpdateTagRequest = {
    value?: string
    color?: string
}
    