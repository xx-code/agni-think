export type TagType = {
    id: string,
    value: string,
    color: string
}


export const useFetchListTags = async (): Promise<Ref<TagType[]>> => { 
    const { data, error } = await useAsyncData('tags', () => fetchListTags())

    if (error.value) {
        const toast = useToast()
        const resData = error.value as ErrorApi
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    } 

    const items = ref(data.value!)
    
    return items
}

export const fetchListTags = async (): Promise<TagType[]> => {
    const response = await $fetch(`${API_LINK}/tags`)
    const data = (response as {data: {id: string, value: string, color: string|null}[]}).data

    return data.map(val => ({id: val.id, value: val.value, color: val.color ?? '#D9D9D9'})) 
}

export const fetchTag = async (tagId: string): Promise<TagType> => {
    const response = await $fetch(`${API_LINK}/tags/${tagId}`)
    const data = (response as {data: {id: string, value: string, color: string|null}}).data

    return {id: data.id, value: data.value, color: data.color ?? '#D9D9D9'}
}

export type CreateTagRequest = {
    value: string,
    color: string
}

export const fetchCreateTag = async (request: CreateTagRequest) => {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/tags`, {
            method: 'POST',
            body: {
                value: request.value,
                color: request.color
            }
        })   
        const data = response as {success: boolean}
        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `Tag ${request.value} cree`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi 
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }
}

export type UpdateTagRequest = {
    tagId: string
    value: string,
    color: string
}
export const fetchUpdateTag = async (request: UpdateTagRequest) => {
    const toast = useToast()
    try {
        const response = await $fetch(`${API_LINK}/tags/${request.tagId}`, {
            method: 'PUT',
            body: {
                value: request.value,
                color: request.color
            } 
        })

        const data = response as {success: boolean}
        if (!data.success) {
            toast.add({ title: 'Oops! Erreur', description: "", color: 'error'})
        }

        toast.add({ title: 'Success', description: `Tag ${request.value} mise a jour`, color: 'success' })
    } catch(err) {
        const resData = err as ErrorApi 
        toast.add({ title: 'Oops! Erreur', description: resData.data.error.message, color: 'error'})
    }

}

export const fetchDeleteTag = async (tagId: string) => {

}