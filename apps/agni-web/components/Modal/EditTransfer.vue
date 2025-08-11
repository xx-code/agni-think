<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent } from '@nuxt/ui';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import type {  EditTransfertType } from '~/types/ui/transaction';
import useAccounts from '~/composables/accounts/useAccounts';

const schema = z.object({
    accountIdFrom: z.string().nonempty('Vous devez selectionner un compte d\'origne'),
    accountIdTo: z.string().nonempty('Vous devez selectionner un compte recepteur'),
    amount: z.number().min(1, 'La somme doit etre plus grand que zero')
})

type Schema = z.output<typeof schema>

const { accountId } = defineProps<{
    accountId?: string 
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditTransfertType ): void    
    (e: 'close', close: boolean): void
}>();

const date = shallowRef(new CalendarDate(new Date().getUTCFullYear(), new Date().getUTCMonth() + 1, new Date().getUTCDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

const {data: accounts} = useAccounts()
const form = reactive({
    accountIdFrom: accountId || '',
    accountIdTo: '',
    amount: 0
})


async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit("submit", {
        accountIdFrom: data.accountIdFrom,
        accountIdTo: data.accountIdTo,
        amount: data.amount,
        date: date.value
    });

    form.accountIdFrom = "";
    form.accountIdTo = "";
    form.amount = 0;

    emit('close', true)
}
</script>

<template>
    <UModal 
        title="Transfert entre comptes">
        <template #body>
            <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
                <UFormField label="Compte de" name="accountIdFrom">
                    <USelect 
                        v-model="form.accountIdFrom"  
                        value-key="value" 
                        :items="accounts?.items.map(acc => ({value: acc.id, label: acc.title}))"
                        class="w-full"
                    />
                </UFormField>

                <UFormField label="Compte vers" name="accountIdTo">
                    <USelect 
                        v-model="form.accountIdTo" 
                        value-key="value" 
                        :items="accounts?.items.map(acc => ({value: acc.id, label: acc.title}))" 
                        class="w-full"
                    />
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