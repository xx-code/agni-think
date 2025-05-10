'use client'

import { useAccountsFetching } from "@/app/hooks/accounts"
import IncreaseAmountSaveGoalForm from "@/components/forms/increaseSaveGoalForm"
import { useRouter } from "next/navigation"
import { useEffect } from "react"

export default function PageSavingAdd({params: {id}}: { params: {id: string}}) {
    const accountHook = useAccountsFetching(false)
    const router = useRouter()

    const renderSaveGoalForm = () => {
    
            if (accountHook.loading) {
                return (
                    <div>Loading...</div>
                )
            }
    
            return (
                <IncreaseAmountSaveGoalForm 
                    accounts={accountHook.accounts} 
                    saveGoalRef={id} 
                    onSubmit={() => router.back()}                
                />
            ) 
        }

    useEffect(() => {
        accountHook.fetchAllAccounts()
    }, [])

    return (
        <div>
            { renderSaveGoalForm() }
        </div>
    )
}