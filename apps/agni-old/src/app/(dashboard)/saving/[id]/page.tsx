'use client'

import SaveGoalForm from "@/app/components/forms/saveGoalForm";
import { CallApiError } from "@/lib/api-clients";
import { SaveGoal } from "@/models/save-goal";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function SaveGoalPage({params: {id}}: { params: {id: string}}) {
    const [saveGoal, setSaveGoal] = useState<SaveGoal>()
    const [loading, setLoading] = useState(false)
    const router = useRouter()

    async function getSaveGoal() {
        setLoading(true)
        try {
            if (id !== 'new-save-goal') {
                let fetchedSaveGoal: any = await axios.get(`/save-goals/${id}`)
                setSaveGoal({
                    saveGoalId: fetchedSaveGoal['id'],
                    name: fetchedSaveGoal['title'],
                    balance: fetchedSaveGoal['balance'],
                    description: fetchedSaveGoal['description'],
                    items: [],
                    target: fetchedSaveGoal['target']
                })
            }
        } catch(error: any) {
            let resError: CallApiError = error
            alert(resError.message) 
        }
        setLoading(false)
    }

    const renderSavingForm = () => {

        if (loading) {
            return (
                <div>Loading...</div>
            )
        }

        return (
            <SaveGoalForm 
                saveGoal={saveGoal} 
                onSubmit={() => router.back()}             
            />
        ) 
    }

    useEffect(() => {
        getSaveGoal()
    }, [])

    return (
        <div>
            {renderSavingForm()}
        </div>
    )
}