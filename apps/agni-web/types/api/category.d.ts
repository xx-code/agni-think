export type GetCategoryResponse = {
    id: string
    title: string
    icon: string
    color: string 
    isSystem?: boolean
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