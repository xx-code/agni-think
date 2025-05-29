<script setup lang="ts">
import * as z from 'zod';
import { reactive } from 'vue';
import { useFetchResumeAccount } from '../../composables/account';
import type { FormSubmitEvent } from '@nuxt/ui';
import { UFormField } from '#components';

const props = defineProps({
    goalId: String,
    isIncrease: Boolean
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

const accounts = useFetchResumeAccount()

type Schema = z.output<typeof schema>
function onSubmit(event: FormSubmitEvent<Schema>) {
    console.log(form)
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
                    <USelect v-model="form.accountId" value-key="id" label-key="title" :items="accounts.filter(acc => acc.id !== ALL_ACCOUNT_ID)" class="w-full" />
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