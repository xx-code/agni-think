export type CategoryType = {
    id: string,
    title: string,
    icon: string,
    color: string
}


export const  useFetchListCategories = async (): Promise<Ref<CategoryType[]>> => {
    const { data, error } = await useAsyncData('categories', () => fetchListCategories())

    
    if (error.value) {
        const toast = useToast()
        const resData = error.value as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 

    const categories = ref(data.value!)

    return categories
}


export async function fetchListCategories(): Promise<CategoryType[]> {
    const response = await $fetch(`${API_LINK}/categories`)
    const data = (response as {data: {categoryId: string, title: string, icon: string, color: string|null}[]}).data
    
    return data.map(val => ({id: val.categoryId, title: val.title, icon: val.icon, color: val.color ?? 'black'}))
}

export async function fetchCategory(accountId: string): Promise<CategoryType> {
    const response = await $fetch(`${API_LINK}/categories/${accountId}`)
    const data = (response as {data: {categoryId: string, title: string, icon: string, color: string|null}}).data

    return {id: data.categoryId, title: data.title, icon: data.icon, color: data.color ?? '#D9D9D9'}
}

export type CreateCategoryRequest = {
    title: string
    icon: string
    color: string
}
export async function fetchCreateCategory(request: CreateCategoryRequest) {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/categories`, {
            method: 'POST',
            body: {
                title: request.title,
                color: request.color,
                icon: request.icon
            }
        })

        const data = response as {success: boolean, data: {accountId: string}}

        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `Categorie ${request.title} cree`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 
}

export type UpdateCategoryRequest = {
    categoryId: string
    title: string
    color: string
    icon: string
}
export async function fetchUpdateCategory(request: UpdateCategoryRequest) {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/categories/${request.categoryId}`, {
            method: 'PUT',
            body: {
                title: request.title,
                color: request.color,
                icon: request.icon
            }
        })

        const data = response as {success: boolean, data: {accountId: string}}

        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `Categorie ${request.title} mis a jour`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi
        console.log(resData)
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}