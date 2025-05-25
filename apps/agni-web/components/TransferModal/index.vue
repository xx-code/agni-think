<script setup lang="ts">
import * as z from 'zod'
import { useFetchResumeAccount } from '../../composables/account';
import { reactive } from 'vue';
import type { FormSubmitEvent } from '@nuxt/ui';

const schema = z.object({
    accountIdFrom: z.string().nonempty('Vous devez selectionner un compte d\'origne'),
    accountIdTo: z.string().nonempty('Vous devez selectionner un compte recepteur'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero')
})

type Schema = z.output<typeof schema>

const props = defineProps({
    accountFromId: String
})

const accounts = useFetchResumeAccount()
const form = reactive({
    accountIdFrom: props.accountFromId ?? '',
    accountIdTo: '',
    amount: 0
})

async function onSubmit(event: FormSubmitEvent<Schema>) {
    console.log('transfert')
}

</script>

<template>
    <UModal 
        :close="{ onClick: () => $emit('close', false)}"
        title="Transfert entre comptes">
        <template #body>
            <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
                <UFormField label="Compte de" name="accountIdFrom">
                    <USelect v-model="form.accountIdFrom"  value-key="id" label-key="title" :items="accounts.filter(acc => acc.id !== ALL_ACCOUNT_ID)"
                        class="w-full"/>
                </UFormField>

                <UFormField label="Compte vers" name="accountIdTo">
                    <USelect v-model="form.accountIdTo" label-key="title" value-key="id" :items="accounts.filter(acc => acc.id !== ALL_ACCOUNT_ID)" 
                        class="w-full"/>
                </UFormField>

                <UFormField label="Argent" name="amount">
                    <UInput v-model="form.amount" type="number" />
                </UFormField>

                <UButton type="submit" label="Transferer" />
            </UForm>
        </template>
    </UModal>
</template>