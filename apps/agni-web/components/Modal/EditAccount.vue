<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import useAccountTypes from '~/composables/internals/useAccountTypes';
import type { AccountBrokeDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType, EditAccountType } from '~/types/ui/account';
import useManagementAccountTypes from '~/composables/internals/useContributionTypes copy';
import useContributionTypes from '~/composables/internals/useManagementAccountTypes';

const { account } = defineProps<{
    account?: AccountWithDetailType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditAccountType, oldValue?: AccountWithDetailType): void    
    (e: 'close', close: boolean): void
}>(); 

const {data: types} = useAccountTypes();
const {data: managementTypes} = useManagementAccountTypes()
const {data: contributionTypes} = useContributionTypes()

const schema = z.object({
    accountName: z.string().nonempty('Le nom du compte est vide'),
    accountType: z.string().nonempty('Vous devez selection un type de compte')
})

type Schema = z.output<typeof schema>

const form = reactive({
    accountName: account?.title || '',
    accountType: account?.type || '',
    managementType: account?.detail ? (account?.detail as AccountBrokeDetailType).managementType ?? undefined : undefined,
    contributionType: account?.detail ? (account?.detail as AccountBrokeDetailType).type ?? undefined : undefined,
    creditLimit: account?.detail ?(account?.detail as AccountCreditDetailType).creditLimit ?? 0 : undefined
})

console.log(managementTypes)


async function onSubmit(event: FormSubmitEvent<Schema>) {
    emit('submit', {
        title: form.accountName,
        type: form.accountType,
        creditLimit: form.creditLimit,
        contributionType: form.contributionType,
        managementType: form.managementType
    }, account);
        
    form.accountName = ""
    form.accountType = ""

    emit('close', true)
};

</script>

<template>
   <UModal 
        :close="{ onClick: () => $emit('close', false)}"
        title="Editeur de compte">
    <template #body>
        <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
            <UFormField label="Nom de compte" name="accountName">
                <UInput v-model="form.accountName" class="w-full"/>
            </UFormField>

            <UFormField label="Type de compte" name="accountType">
                <USelect 
                    v-model="form.accountType" 
                    value-key="value" 
                    :items="types?.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                </USelect>
            </UFormField>

            <UFormField label="Limite de credit" v-if="form.accountType === 'CreditCard'">
                <UInput v-model="form.creditLimit" class="w-full"/>
            </UFormField>

            <div v-else-if="form.accountType === 'Broking'">
                <UFormField label="Type de compte de contribution">
                    <USelect 
                        v-model="form.contributionType" 
                        value-key="value" 
                        :items="contributionTypes?.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                    </USelect>
                </UFormField>

                <UFormField label="Type de management">
                    <USelect 
                        v-model="form.managementType" 
                        value-key="value" 
                        :items="managementTypes?.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                    </USelect>
                </UFormField> 
            </div>

            <UButton type="submit" :label="account ? 'Mettre a jour' : 'Ajouter nouveau'"/>
        </UForm>
    </template>
   </UModal> 
</template>