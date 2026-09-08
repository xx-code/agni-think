export type GetTagResponse = {
    id: string
    value: string
    color?: string
    system: boolean
    archived: boolean
}

export type CreateTagRequest = {
    value: string
    color: string
}

export type UpdateTagRequest = {
    value?: string
    color?: string
}
    
export type ArchiveTagRequest = {
    archive: boolean
}

export type QueryFilterTagRequest = QueryFilterRequest & {
    isSystem: boolean?,
    isArchived: boolean?
}