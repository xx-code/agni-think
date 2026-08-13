<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import { UFormField } from '#components';
import type { EditUpdateAmountFundType, FundType } from '~/types/ui/fund';
import { fetchAccounts } from '~/composables/api/accounts';
import { useUpdateAmountFund } from '~/composables/api/funds';

const { fund, fundAccountId, isIncrease } = defineProps<{
    fund: {id: string, title: string }
    fundAccountId?: string
    isIncrease: boolean
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditUpdateAmountFundType, isIncrease: boolean, oldValue?: FundType): void    
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
    const res = fetchAccounts({ offset: 0, limit:0, queryAll: true})
    return (await res).items
})

type Schema = z.output<typeof schema>

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data
    try {
        await useUpdateAmountFund({
            isIncrease: isIncrease,
            amount: data.amount,
            accountId: data.accountId,
            fundId: fund.id
        })

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