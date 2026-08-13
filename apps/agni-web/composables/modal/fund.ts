import type { FundForm } from "~/types/form/fund"
import { useCreateFund } from "../api/funds"
import { fundFormToCreateFundRequest } from "~/mappers/fund"

export const useFundModal = () => {
    const isOpen = useState('fundModal:isOpen', () => false)
    const isLoading = useState('fundModal:isLoading', () => false)
    const error = useState<string | null>('fundModal:isLoading', () => null)

    const formState = reactive<FundForm>({
        target: 0,
        title: "",
        description: ""
    })

    const openFund = (initialData?: Partial<FundForm>) => {
        if (initialData) {
            Object.assign(formState, initialData)
        }

        error.value = null
        isOpen.value = true
    }

    const close = () => {
        isOpen.value = false
    }

    const submit = async () => {
        isLoading.value = true
        error.value = null
        try {
            await useCreateFund(fundFormToCreateFundRequest(formState))
            close()
        } catch(err: any) {
            error.value = err.message  || 'Une erreur est survenue'
        } finally {
            isLoading.value = false
        }
    }
    
    return {
        isOpen,
        isLoading,
        error,
        formState,
        close,
        openFund,
        submit
    }
}