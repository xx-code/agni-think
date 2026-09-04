import type { GetCategoryResponse } from "../api/category"

export type EditCategoryType = {
    title: string
    icon: string
    color: string
}

export type CategoryType = Omit<GetCategoryResponse, 'system' | 'archive'> & {
    isSystem: boolean,
    isArchive: boolean
}  