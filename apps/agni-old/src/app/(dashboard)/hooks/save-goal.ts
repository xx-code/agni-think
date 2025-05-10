import { SaveGoalModel } from "@/api/models/save-goal";
import { api, CallApiError } from "@/lib/api-clients";
import axios from "axios";
import { useState } from "react";

export function useSaveGoals() {
    const [saveGoals, setSaveGoals] = useState<SaveGoalModel[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<CallApiError|null>(null)
    
    const fetchSaveGoal = async () => {
        setLoading(true)
        try {
            let fetchedSaveGoals: any[] = await api.get('/save-goals')
            let saveGoals: SaveGoalModel[] = []

            for (let fetchedSaveGoal of fetchedSaveGoals) {
                saveGoals.push({
                    id: fetchedSaveGoal['id'],
                    title: fetchedSaveGoal['title'],
                    balance: fetchedSaveGoal['balance'],
                    description: fetchedSaveGoal['description'],
                    target: fetchedSaveGoal['target'],
                    items: []
                })
            }

            setSaveGoals(saveGoals)
        } catch(error: any) {
            let apiError: CallApiError = error
            setError(apiError)
        }
        setLoading(false)
    }

    return { saveGoals, loading, error, fetchSaveGoal }
}