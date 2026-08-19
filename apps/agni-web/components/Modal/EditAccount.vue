<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import type { AccountBrokeDetailType, AccountCreditDetailType, Account, AccountWithDetailType, EditAccount } from '~/types/ui/account';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import { fetchAccounts, fetchManagementAccounts, fetchContributionTypes } from '~/composables/api/internal';

const { account } = defineProps<{
    account?: AccountWithDetailType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditAccount, oldValue?: AccountWithDetailType): void    
    (e: 'close', close: boolean): void
}>(); 

const { data: utils } = useAsyncData('utils+edit-account', async () => {
    const [Accounts, managementTypes, contributionTypes] = await Promise.all(
        [ 
            fetchAccounts(), 
            fetchManagementAccounts(),
            fetchContributionTypes()
        ]
    )

    return {
        Accounts,
        managementTypes,
        contributionTypes
    } 
})

const schema = z.object({
    accountName: z.string().nonempty('Le nom du compte est vide'),
    Account: z.string().nonempty('Vous devez selection un type de compte'),
    color: z.string().nonempty('Vous devez ajouter une couleur')
})

type Schema = z.output<typeof schema>

const form = reactive({
    accountName: account?.title || '',
    color: account?.color || '',
    Account: account?.type || '',
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
        type: form.Account,
        color: form.color,
        creditLimit: form.creditLimit,
        contributionType: form.contributionType,
        managementType: form.managementType,
        invoiceDate: invoiceDate.value 
    }, account);
        
    form.color = ""
    form.accountName = ""
    form.Account = ""
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

            <UFormField label="Type de compte" name="Account">
                <USelect 
                    v-model="form.Account" 
                    value-key="value" 
                    :items="utils?.Accounts.map(i => ({ label: i.value, value: i.id}))" class="w-full">
                </USelect>
            </UFormField>

            <UFormField label="Couleur" name="color">
                <div class="flex items-center gap-1">
                    <div class="rounded-md" :style="'width: 25px; height: 25px;'+'background-color:'+form.color+';'"></div>
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-palette" >Color Picker</UButton>
                        <template #content>
                            <UColorPicker v-model="form.color" />
                        </template>
                    </UPopover>
                </div>
            </UFormField>

            <div v-if="form.Account === 'CreditCard'">
                <UFormField label="Limite de credit" v-if="form.Account === 'CreditCard'">
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

            <div v-else-if="form.Account === 'Broking'">
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