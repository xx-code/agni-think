'use client'

import CategoryForm from "@/app/components/forms/categoryForm"
import { CallApiError } from "@/lib/api-clients"
import { Category } from "@/models/category"
import axios from "axios"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"

export default function CategoryPage({params: {id}}: { params: {id: string}}) {
    const [category, setCategory] = useState<Category>()
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<any|null>(null)
    const router = useRouter()

    const fetchCategory = async () => {
        setLoading(true)
        try {
            if (id !== 'new') {
                let fetchedCategory: any = await axios.get(`/categories/${id}`)
                setCategory({
                    categoryId: fetchedCategory['id'],
                    name: fetchedCategory['title'],
                    color: fetchedCategory['color'],
                    icon: fetchedCategory['icon']
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
            <CategoryForm 
                currentCategory={category} 
                onSave={() => router.back()} 
            />
        ) 
    }

    useEffect(() => {
        fetchCategory()
    }, [])

    return (
        <div>
            {renderForm()}
        </div>
    )
}