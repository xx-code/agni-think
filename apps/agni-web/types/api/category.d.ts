import type { QueryFilterRequest } from "."

export type GetCategoryResponse = {
    id: string
    title: string
    icon: string
    color: string 
    archive: boolean
    system?: boolean
}

export type CreateCategoryRequest = {
    title: string,
    icon: string,
    color: string
}

export type UpdateCategoryRequest = {
    title?: string
    icon?: string 
    color?: string
}

export type ArchiveCategoryRequest = {
    archive: boolean
}

export type QueryFilterCategoryRequest = QueryFilterRequest & {
    isSystem: boolean?,
    isArchived: boolean?
}