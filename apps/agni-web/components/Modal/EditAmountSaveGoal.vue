<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import { UFormField } from '#components';
import useAccounts from '~/composables/accounts/useAccounts';
import type { EditUpdateAmountSaveGoalType, SaveGoalType } from '~/types/ui/saveGoal';

const { saveGoal, isIncrease } = defineProps<{
    saveGoal?: SaveGoalType
    isIncrease: boolean
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditUpdateAmountSaveGoalType, isIncrease: boolean, oldValue?: SaveGoalType): void    
    (e: 'close', close: boolean): void
}>(); 

const schema = z.object({
    accountId: z.string().nonempty("Vous devez selectionner un compte crediteur ou debiteur"),
    amount: z.number().min(1, "Vous devez credit ou debiter une somme superieux a zero") 
})

const form = reactive({
    accountId: "",
    isIncrease: isIncrease ? '0' : '1',
    amount: 0
})

const {data: accounts} = useAccounts({
    offset: 0,
    limit: 0,
    queryAll: true
})

type Schema = z.output<typeof schema>

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data
    emit('submit', {
        accountId: data.accountId,
        amount: data.amount 
    }, isIncrease, saveGoal);

    form.accountId = ''
    form.amount = 0

    emit('close', true)
}

</script>

<template>
    <UModal title="Ajouter ou diminuer but d'epargne">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField>
                    <UTabs v-model="form.isIncrease" :content="false" :items="[{label:'Ajouter'}, {label:'Retirer'}]" class="w-full"/> 
                </UFormField>
                <UFormField label="Compte" name="accountId">
                    <USelect 
                        v-model="form.accountId" 
                        value-key="id" 
                        label-key="title" 
                        :items="accounts?.items.map(acc => ({id: acc.id, title: acc.title}))" 
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