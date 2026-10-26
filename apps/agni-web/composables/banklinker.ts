import type { PlaidLinkOnSuccessMetadata, PlaidLinkOptions } from "@jcss/vue-plaid-link"
import { bankRegisterResponseToBankRegister } from "~/mappers/bankRegister"
import { API_ROUTES } from "~/shared/routes"
import type {  CreateBankRegisterRequest, ExchangePublicTokenResponse, GetBankRegisterResponse, PlaidAccessCodeRequest, PlaidAccessCodeResponse, UpdateBankRegisterRequest } from "~/types/api/bank-register"

import { ref, computed } from 'vue'
import type { BankRegisterType, PlaidAccessCode } from "~/types/ui/bank-register"

export function useBankLinker(callBack?: () => void) {
    const toast = useToast()
    const token = ref<string | null>(null)
    const isReady = computed(() => token.value != null)

    const createLink = async () => {
        try {
            const res = await ApiLinkBuilder
                .route<ExchangePublicTokenResponse>(API_ROUTES.BANK_REGISTERS.CREATE_BANK_LINKER)
                .execute()

            token.value = res.publicToken
        } catch (err: any) {
            toast.add({
                title: 'Erreur lors de la création du lien',
                description: err.message,
                color: 'error'
            })
        }
    }

    const handleSuccess = async (publicToken: string, metadata: PlaidLinkOnSuccessMetadata) => {
        try {
            const res = await ApiLinkBuilder
                .route<PlaidAccessCodeResponse>(API_ROUTES.BANK_REGISTERS.EXCHANGE_PUBLIC_TOKEN)
                .body({ publicToken } satisfies PlaidAccessCodeRequest)
                .mapper((data) => ({ accessCode: data.accessCode } satisfies PlaidAccessCode))
                .execute()

            const institutionId = metadata.institution?.institution_id ?? ""

            let register: BankRegisterType | null = null
            if (institutionId) {
                try {
                    register = await ApiLinkBuilder
                        .route<GetBankRegisterResponse>(API_ROUTES.BANK_REGISTERS.GET_BANK_REGISTER_ACCESS_CODE)
                        .params({ id: institutionId })
                        .mapper(bankRegisterResponseToBankRegister)
                        .execute()
                } catch {
                    register = null
                }
            }

            if (register && register.id) {
                await ApiLinkBuilder
                    .route(API_ROUTES.BANK_REGISTERS.UPDATE_BANK_REGISTER)
                    .params({ id: register.id })
                    .body({
                        accounts: metadata.accounts.map(i => ({
                        accountId: register.accounts.find(acc => acc.bankAccountId === i.id)?.accountId,
                        bankAccountId: i.id,
                        bankName: i.name
                    }))} satisfies UpdateBankRegisterRequest)
                    .execute()
            } else {
                await ApiLinkBuilder
                    .route(API_ROUTES.BANK_REGISTERS.CREATE_BANK_REGISTER)
                    .body({
                        title: metadata.institution?.name ?? "Banque",
                        institutionId: institutionId,
                        accessCode: res.accessCode,
                        accounts: metadata.accounts.map(i => ({
                            bankAccountId: i.id,
                            bankName: i.name
                        }))
                    } satisfies CreateBankRegisterRequest)
                .execute()
            }

            if (callBack) callBack()

        } catch (err: any) {
            toast.add({
                title: 'Erreur',
                description: err.message,
                color: 'error'
            })
        }
    }


    const config = computed<PlaidLinkOptions>(() => ({
        token: token.value,
        onSuccess: handleSuccess,
    }))

    return {
        token,
        isReady,
        createLink,
        config
    }
}