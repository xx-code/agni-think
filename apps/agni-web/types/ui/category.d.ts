export type EditCategoryType = {
    title: string
    icon: string
    color: string
}

export type CategoryType = {
    id: string
    title: string
    icon: string
    color?: string 
    isSystem: boolean
}