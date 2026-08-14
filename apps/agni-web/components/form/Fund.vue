<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { FundForm } from '~/types/form/fund';
import type { AccountType } from '~/types/ui/account';

const form = defineModel<Partial<FundForm>>()
const { accounts, validate } = defineProps<{
    accounts: AccountType[]
    validate: (state: Partial<FundForm>) => FormError[]
}>()
const emit = defineEmits<{
    submit: [event: FormSubmitEvent<FundForm>],
    close: []
}>()
</script>

<template>
    <UForm v-if="form" :validate="validate" :state="form" @submit="e => emit('submit', e)" class="space-y-4">
        <UFormField label="Nom Fond" name="title">
            <UInput v-model="form.title" class="w-full" />
        </UFormField>
        <UFormField label="Petit description" name="description">
            <UTextarea v-model="form.description" autoresize class="w-full" />
        </UFormField>
        <UFormField label="Montant du fond" name="targetAmount">
            <UInput v-model="form.target" class="w-full" type="number" />
        </UFormField>


        <UFormField label="Compte lie au fond" name="accountId" >
            <USelect 
                v-model="form.accountId" 
                value-key="value" 
                :items="accounts?.map(i => ({value: i.id, label: i.title}))" 
                class="w-full" />
        </UFormField>

        <UButton label="Submit" type="submit" />
    </UForm>
</template>