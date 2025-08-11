export type GetCategoryResponse = {
    categoryId: string
    title: string
    icon: string
    color?: string 
    isSystem: boolean
}

export type GetAllCategoriesResponse = {
    categoryId: string
    title: string
    icon: string
    color?: string 
    isSystem: boolean
}

export type CreateCategoryRequest = {
    title: string,
    icon: string,
    color?: string
    isSystem?: boolean
}

export type UpdateCategoryRequest = {
    title?: string
    icon?: string 
    color?: string
}