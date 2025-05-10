'use client'

import TagForm from "@/app/components/forms/tagForm/tag"
import { api, CallApiError } from "@/lib/api-clients"
import { Tag } from "@/models/tag"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"

export default function TagPage({params: {id}}: { params: {id: string}}) {
    const [tag, setTag] = useState<Tag>()
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<any|null>(null)
    const router = useRouter()

    const fetchTag = async () => {
        setLoading(true)
        try {
            if (id !== 'new') {
                let fetchedTag: any = await api.get(`/tags/${id}`)
                setTag({
                    tagId: fetchedTag['id'],
                    color: fetchedTag['color'],
                    value: fetchedTag['value']
                })
            }
        } catch(error: any) {
            let resError: CallApiError = error
            setError(resError.message) 
        }
        setLoading(false)
    }

    const renderForm = () => {
        if(loading) {
            return <div>Loading...</div>
        }

        if (error) {
            return <div>{error}</div>
        }

        return (
            <TagForm 
                currentTag={tag} 
                onSave={() => router.back()} 
            />
        ) 
    }

    useEffect(() => {
        fetchTag()
    }, [])

    return (
        <div>
            {renderForm()}
        </div>
    )
}