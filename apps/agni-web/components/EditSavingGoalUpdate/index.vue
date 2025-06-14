<script setup lang="ts">
import * as z from 'zod';
import { reactive } from 'vue';
import { fetchListAccounts, useFetchListAccount, useFetchResumeAccount } from '../../composables/account';
import type { FormSubmitEvent } from '@nuxt/ui';
import { UFormField } from '#components';
import { fetchDescreaseSaveGoal, fetchIncreaseSaveGoal } from '../../composables/goals';

const props = defineProps({
    goalId: String,
    isIncrease: Boolean,
    onSaved: Function
})

const schema = z.object({
    accountId: z.string().nonempty("Vous devez selectionner un compte crediteur ou debiteur"),
    amount: z.number().min(1, "Vous devez credit ou debiter une somme superieux a zero") 
})

const form = reactive({
    accountId: "",
    isIncrease: props.isIncrease ? '0' : '1',
    amount: 0
})

const {data: accounts} = useFetchListAccount()

type Schema = z.output<typeof schema>

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (form.isIncrease) 
        await fetchIncreaseSaveGoal({accountFromId: form.accountId, amount: form.amount, saveGoalId: props.goalId!})
    else 
        await fetchDescreaseSaveGoal({accountToId: form.accountId, amount: form.amount, saveGoalId: props.goalId!})

    if (props.onSaved) props.onSaved()

    emit('close')
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
                    <USelect v-model="form.accountId" value-key="id" label-key="title" :items="accounts ? accounts.map(acc => ({id: acc.id, title: acc.title})) : []" class="w-full" />
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