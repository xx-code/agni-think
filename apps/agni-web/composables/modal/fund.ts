import type { FundForm } from "~/types/form/fund"
import type { CreatedRequest } from "~/types/api"
import type { CreateFundRequest } from "~/types/api/fund"
import { fundFormToCreateFundRequest } from "~/mappers/fund"
import { ApiLinkBuilder } from "~/utils/ApiLinkBuilder"
import { API_ROUTES } from "~/shared/routes"

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
            await ApiLinkBuilder
                .route<CreatedRequest>(API_ROUTES.FUNDS.CREATE_FUND)
                .body(fundFormToCreateFundRequest(formState))
                .execute()
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
