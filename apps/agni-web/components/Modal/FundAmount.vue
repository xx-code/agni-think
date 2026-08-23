<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import { UFormField } from '#components';
import type { EditUpdateAmountFund, Fund } from '~/types/ui/fund';
import type { ListResponse } from '~/types/api';
import type { GetAccountResponse } from '~/types/api/account';
import { listAccountsToListAccount } from '~/mappers/account';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';

const { fund, fundAccountId, isIncrease } = defineProps<{
    fund: {id: string, title: string }
    fundAccountId?: string
    isIncrease: boolean
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditUpdateAmountFund, isIncrease: boolean, oldValue?: Fund): void    
    (e: 'close', close: boolean): void
}>(); 

const toast = useToast()

const schema = z.object({
    accountId: z.string().nonempty("Vous devez selectionner un compte crediteur ou debiteur"),
    amount: z.number().min(1, "Vous devez credit ou debiter une somme superieux a zero") 
})

const form = reactive({
    accountId: fundAccountId,
    isIncrease: isIncrease ? '0' : '1',
    amount: 0
})

const {data: accounts} = useAsyncData('editAmountSaving+accounts', async () => {
    const res = await ApiLinkBuilder
        .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
        .query({ offset: 0, limit: 0, queryAll: true })
        .mapper(listAccountsToListAccount)
        .execute()
    return res.items
})

type Schema = z.output<typeof schema>

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data
    try {
        const body = {
            id: fund.id,
            accountId: data.accountId,
            amount: data.amount
        }

        await ApiLinkBuilder
            .route(isIncrease ? API_ROUTES.FUNDS.INCREASE_FUND : API_ROUTES.FUNDS.DECREASE_FUND)
            .params({ id: fund.id })
            .body(body)
            .execute()

        toast.add({
            title: 'Succès',
            description: isIncrease ? 'Montant ajouté' : 'Montant retiré',
            color: 'success'
        })
        emit('close', true)
    } catch(err: any) {
        toast.add({
            title: 'Erreur',
            description: "Erreur lors de la mise à jour: " + err.message,
            color: 'error'
        })
    }
}

</script>

<template>
    <UModal :title="'Ajouter ou diminuer but Fond' + fund.title">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField>
                    <UTabs v-model="form.isIncrease" :content="false" :items="[{label:'Ajouter'}, {label:'Retirer'}]" class="w-full"/> 
                </UFormField>
                <UFormField label="Compte" name="accountId">
                    <USelect 
                        :disabled="fundAccountId ? true : false"
                        v-model="form.accountId" 
                        value-key="id" 
                        label-key="title" 
                        :items="accounts?.map(acc => ({id: acc.id, title: acc.title}))" 
                        class="w-full" 
                    />
                </UFormField>
                <UFormField label="Somme" name="amount">
                    <UInput v-model="form.amount" type="number" />
                </UFormField>
                <UFormField>
                    <UButton label="Envoyer" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal>    
</template>