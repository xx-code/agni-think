<script setup lang="ts">
import * as z from 'zod'
import { useFetchResumeAccount } from '../../composables/account';
import { reactive, shallowRef } from 'vue';
import type { FormSubmitEvent } from '@nuxt/ui';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import { fetchTransfertBetweenAccount } from '../../composables/transactions';

const schema = z.object({
    accountIdFrom: z.string().nonempty('Vous devez selectionner un compte d\'origne'),
    accountIdTo: z.string().nonempty('Vous devez selectionner un compte recepteur'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero')
})

type Schema = z.output<typeof schema>

const props = defineProps({
    accountFromId: String,
    onSaved: Function
})

const date = shallowRef(new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

const accounts = await useFetchResumeAccount()
const form = reactive({
    accountIdFrom: props.accountFromId ?? '',
    accountIdTo: '',
    amount: 0
})

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    await fetchTransfertBetweenAccount({
        accountFromId: form.accountIdFrom,  
        accountToId: form.accountIdTo, 
        date: date.value.toString(), amount: form.amount})

    if (props.onSaved) props.onSaved()

    emit('close')
}

</script>

<template>
    <UModal 
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

                <UFormField label="Date" name="date">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ date ? df.format(date.toDate(getLocalTimeZone())) : 'Selectionnez une date' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="date" />
                        </template>
                    </UPopover>
                </UFormField>

                <UButton type="submit" label="Transferer" />
            </UForm>
        </template>
    </UModal>
</template>