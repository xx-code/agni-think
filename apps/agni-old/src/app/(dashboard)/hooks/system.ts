import { TagModel } from "@/api/models/tag"
import { api, CallApiError } from "@/lib/api-clients"
import { Category } from "@/models/category"
import { Tag } from "@/models/tag"
import axios from "axios"
import { useState } from "react"

export function useCategories() {
    const [categories, setCategories] = useState<Category[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setErrorCategories] = useState<any|null>(null)

    const fetchCategories = async () => {
        setLoading(true)
        try {
            let fetchedCategories: any[] = await api.get('/categories')
            let categories: Category[] = []
            for(let fetchedCategory of fetchedCategories) {
                categories.push({
                    categoryId: fetchedCategory['categoryId'],
                    name: fetchedCategory['title'],
                    color: fetchedCategory['icon'],
                    icon: fetchedCategory['color']
                })
            }
            setCategories(categories)
        } catch(error: any) {
            let apiError: CallApiError = error
            setErrorCategories(apiError)
        }
        setLoading(false)
    }

    return {categories, loading, error, fetchCategories}
}

export function useTags() {
    const [tags, setTags] = useState<Tag[]>([])
    const [loading, setLoading] = useState<boolean>(true)
    const [error, setErrorTags] = useState<any|null>(null)

    const fetchTags = async () => {
        setLoading(true)
        try {
            let fetchedTags: any[] = await api.get('/tags')
            let tags: Tag[] = []
            for(let fetchedTag of fetchedTags) {
                tags.push({
                    tagId: fetchedTag['id'],
                    color: fetchedTag['color'],
                    value: fetchedTag['value']
                })
            }
            setTags(tags)
        } catch(err: any) {
            let apiError: CallApiError = error
            setErrorTags(apiError)
        }
        setLoading(false)
    }

    return {tags, loading, error, fetchTags}
}