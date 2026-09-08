<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import { listAccountsToListAccount } from '~/mappers/account';
import { listBankRegistersResponseToListBankRegisters } from '~/mappers/bankRegister';
import { API_ROUTES } from '~/shared/routes';
import type { ListResponse } from '~/types/api';
import type { GetAccountResponse } from '~/types/api/account';
import type { GetBankRegisterResponse, UpdateBankRegisterRequest } from '~/types/api/bank-register';
import type { EditAddBankRegister } from '~/types/ui/bank-register';

const { accountId } = defineProps<{
    accountId?: string
}>()

const emit = defineEmits<{
    (e: 'close', refresh: boolean): void
}>();

const toast = useToast()
const form = ref<Partial<EditAddBankRegister>>({
    bankRegisterId: undefined,
    accountId: accountId,
    bankRegisterAccountId: undefined
})
const { start, stop } = useLoading()

const { data, refresh } = useAsyncData("banking+all+register+link+bank", async () => {
    const [ bankRegisters, accounts ] = await Promise.all([
        ApiLinkBuilder
            .route<ListResponse<GetBankRegisterResponse>>(API_ROUTES.BANK_REGISTERS.GET_BANK_REGISTERS)
            .query({ offset: 0, limit:1, queryAll: true })
            .mapper(listBankRegistersResponseToListBankRegisters)
            .execute(),
        ApiLinkBuilder
            .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
            .query({ offset: 0, limit: 1, queryAll: true})
            .mapper(listAccountsToListAccount)
            .execute()
    ])

    return  {
        bankRegisters: bankRegisters.items,
        accounts: accounts.items
    }
})

function validate(state: Partial<EditAddBankRegister>): FormError[] {
    const errors: FormError[] = []

    if (!state.accountId) errors.push({name: 'accountId', message: 'Require'})

    if (!state.bankRegisterId) errors.push({name: 'bankRegisterId', message: 'Require'})

    return errors
}

const getBankRegister = computed(() => {
    if (!data.value)
        return []

    const bankRegisterAccounts = data.value.bankRegisters
        .find(i => i.id === form.value.bankRegisterId)?.accounts
        .filter(i => i.accountId === null || i.accountId === undefined)
        .map(i => ({ label: i.bankName, value: i.bankAccountId}))
    
    return bankRegisterAccounts
}) 

async function onSubmit(event: FormSubmitEvent<EditAddBankRegister>) {
    const bankInput = event.data

    try {
        start()
        const bankAccounts = data.value?.bankRegisters.find(i => i.id === bankInput.bankRegisterId)?.accounts

        if (bankAccounts) {
            const indexAccount = bankAccounts.findIndex(i => i.bankAccountId === bankInput.bankRegisterAccountId)
            if (indexAccount >= 0 && bankAccounts.length > 0) {
                bankAccounts.splice(indexAccount, 1, {
                    accountName: bankAccounts[indexAccount]!.accountName,
                    bankAccountId: bankInput.bankRegisterAccountId,
                    bankName: bankAccounts[indexAccount]!.accountName,
                    isActive: bankAccounts[indexAccount]!.isActive,
                    accountId: bankInput.accountId 
                })


                await ApiLinkBuilder
                    .route(API_ROUTES.BANK_REGISTERS.UPDATE_BANK_REGISTER)
                    .params({ id: bankInput.bankRegisterId })
                    .body({
                        accounts: bankAccounts.map(i => ({ accountId: i.accountId, bankName: i.bankName, bankAccountId: i.bankAccountId}))
                    } as UpdateBankRegisterRequest)
                    .execute()
                await ApiLinkBuilder
                    .route(API_ROUTES.BANK.INIT_TRANSACTION)
                    .execute()
                emit("close", true)
            } 
        }
    } catch (err: any) {
        stop()
        toast.add({
            title: err?.error,
            description: err?.message,
            color: 'error'
        })
    } finally {
        stop()
    }
}
  

</script>

<template>
    <UModal>
        <template #body>
            <UForm class="space-y-2" :validate="validate" :state="form" @submit="onSubmit">
                <UFormField label="Institut bancaire" name="bankRegister">
                    <USelectMenu
                        class="w-full" 
                        :items="data?.bankRegisters?.map(i => ({ label: i.title, value: i.id })) ?? []"
                        value-key="value"
                        v-model="form.bankRegisterId"
                    />
                </UFormField>
                
                <UFormField label="Compte apps" name="accountId">
                    <USelectMenu
                        class="w-full" 
                        :items="data?.accounts?.map(i => ({ label: i.title, value: i.id})) ?? []"
                        value-key="value"
                        v-model="form.accountId"
                    />
                </UFormField>
                
                <UFormField label="Compte bancaire" name="bankRegisterAccountId">
                    <USelectMenu 
                        v-if="form.accountId && form.bankRegisterId"
                        class="w-full"
                        :items="getBankRegister"
                        value-key="value"
                        v-model="form.bankRegisterAccountId"
                    />
                </UFormField>
                

                <div class="w-full items-center">
                    <UButton  
                        label="Ajouter"
                        type="submit"
                    />
                </div>
            </UForm> 
        </template>
    </UModal>
</template>