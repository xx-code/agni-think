<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import type { AccountBrokeDetailType, AccountCreditDetailType, AccountType, AccountWithDetailType, EditAccountType } from '~/types/ui/account';
import { fetchAccountTypes } from '~/composables/internals/useAccountTypes';
import { fetchManagementAccountTypes } from '~/composables/internals/useManagementAccountTypes';
import { fetchContributionTypes } from '~/composables/internals/useContributionTypes';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';

const { account } = defineProps<{
    account?: AccountWithDetailType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditAccountType, oldValue?: AccountWithDetailType): void    
    (e: 'close', close: boolean): void
}>(); 

const { data: utils } = useAsyncData('utils+edit-account', async () => {
    const [accountTypes, managementTypes, contributionTypes] = await Promise.all(
        [ 
            fetchAccountTypes(), 
            fetchManagementAccountTypes(),
            fetchContributionTypes()
        ]
    )

    return {
        accountTypes,
        managementTypes,
        contributionTypes
    } 
})

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
    creditLimit: account?.detail ?(account?.detail as AccountCreditDetailType).creditLimit ?? 0 : undefined,
})

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});
let invoiceRawDate: Date|undefined = account?.detail ? (account?.detail as AccountCreditDetailType).nextInvoicePaymentDate : new Date()
const invoiceDate = shallowRef(invoiceRawDate ? new CalendarDate(invoiceRawDate.getFullYear(), invoiceRawDate.getMonth() + 1, invoiceRawDate.getDate()) : undefined)


async function onSubmit(event: FormSubmitEvent<Schema>) {
    emit('submit', {
        title: form.accountName,
        type: form.accountType,
        creditLimit: form.creditLimit,
        contributionType: form.contributionType,
        managementType: form.managementType,
        invoiceDate: invoiceDate.value 
    }, account);
        
    form.accountName = ""
    form.accountType = ""
    form.contributionType = ""
    invoiceDate.value = undefined

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
                    :items="utils?.accountTypes.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                </USelect>
            </UFormField>

            <div v-if="form.accountType === 'CreditCard'">
                <UFormField label="Limite de credit" v-if="form.accountType === 'CreditCard'">
                    <UInput v-model="form.creditLimit" class="w-full"/>
                </UFormField>
                <UFormField label="" name="wishDueDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ invoiceDate ? df.format(invoiceDate.toDate(getLocalTimeZone())) : 'Selectionnez date de facture' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="invoiceDate" />
                        </template>
                    </UPopover>
                </UFormField>
            </div> 

            <div v-else-if="form.accountType === 'Broking'">
                <UFormField label="Type de compte de contribution">
                    <USelect 
                        v-model="form.contributionType" 
                        value-key="value" 
                        :items="utils?.contributionTypes.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                    </USelect>
                </UFormField>

                <UFormField label="Type de management">
                    <USelect 
                        v-model="form.managementType" 
                        value-key="value" 
                        :items="utils?.managementTypes.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                    </USelect>
                </UFormField> 
            </div>

            <UButton type="submit" :label="account ? 'Mettre a jour' : 'Ajouter nouveau'"/>
        </UForm>
    </template>
   </UModal> 
</template>