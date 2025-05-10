'use client'

import { useEffect, useState } from "react";
import { useCategories, useTags } from "../../hooks/system";
import { useRouter } from "next/navigation";
import { BudgetDetail, convertStringToPeriod } from "@/models/budget";
import { api } from "@/lib/api-clients";
import BudgetForm from "@/app/components/forms/budgetForm";

export default function Page({params: {id}}: { params: {id: string}}) {
    const hookCategory = useCategories()
    const hookTag = useTags()
    const [budget, setBudget] = useState<BudgetDetail>()
    const [loading, setLoading] = useState(true)
    const router = useRouter()

    async function fetchBudget() {
        setLoading(true)
        try {
            if (id !== 'new-budget') {
                let fetchedBudget: any = await api.get(`/api/budget/${id}`)

                setBudget({
                    id: fetchedBudget['id'],
                    title: fetchedBudget['title'],
                    target: fetchedBudget['target'],
                    currentBalance: fetchedBudget['currentBalance'],
                    period: convertStringToPeriod(fetchedBudget['period']),
                    categories: fetchedBudget['categories'].map((cat: any) => ({categoryId: cat['id'], name: cat['title'], icon: cat['icon'], color: cat['color']})),
                    tags: fetchedBudget['tags'].map((tag: any) => ({tagId: tag['id'], name: tag['title'], color: tag['color']})),
                    endDate: fetchedBudget['endDate'],
                    periodTime: fetchedBudget['periodTime'],
                    startDate: fetchedBudget['startDate'],
                    updateDate: fetchedBudget['updateDate'],
                })
            }
        } catch(error: any) {
            if (error.response) {
                alert(error.response.data)
            } else if (error.request) {
                alert(error.request)
            } else {
                let unknowError = {message: 'Error unknowType', error}
                alert(unknowError)
            }
        }
        setLoading(false)
    }

    useEffect(() => {
        hookCategory.fetchCategories()
        hookTag.fetchTags()
        fetchBudget()
    }, [])

    const renderBudgetForm = () => {
        if (hookCategory.error) {
            return (
                <div>
                    Error Category: {hookCategory.error}
                </div>
            )
        }

        if (hookTag.error) {
            return (
                <div>
                    Error tag: {hookCategory.error}
                </div>
            )
        }

        if (loading) {
            return (
                <div>Loading...</div>
            )
        }

        return (
            <BudgetForm 
                currentBudget={budget} 
                categories={hookCategory.categories} 
                tags={hookTag.tags} 
                onSave={() => router.back()}
            />
        ) 
    }

    return (
        <div>
            {
                renderBudgetForm()
            }
        </div>
    )
}