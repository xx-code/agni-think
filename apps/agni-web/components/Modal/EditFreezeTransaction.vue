<script lang="ts" setup>
import * as z from 'zod'
import { reactive, shallowRef } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import useAccounts from '~/composables/accounts/useAccounts';
import type { EditFreezeTransactionType, TransactionType } from '~/types/ui/transaction';

const { accountId } = defineProps<{
    accountId?: string
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditFreezeTransactionType): void    
    (e: 'close', close: boolean): void
}>();

const schema = z.object({
    accountId: z.string().nonempty('Vous devez selection un compte'),
    amount: z.number().min(1, 'Vous de avoir un prix superieux a zero')
})

type Schema = z.output<typeof schema>

const {data: accounts} = useAccounts()

const form = reactive({
    accountId: accountId || '',
    amount: 0
})

const date = shallowRef(new CalendarDate(new Date().getUTCFullYear(), new Date().getUTCMonth() + 1, new Date().getUTCDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})


async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', { 
        accountId: data.accountId,
        amount: data.amount,
        endDate: date.value 
    });

    form.accountId = "";
    form.amount = 0;
    
    emit('close', false);
}

</script>

<template>
    <UModal title="Freeze de transaction">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Compte" name="accountId">
                    <USelect 
                        v-model="form.accountId" 
                        value-key="id" 
                        label-key="title" 
                        :items="accounts?.items.map(acc => ({id: acc.id, title: acc.title}))" 
                        class="w-full"
                    />
                </UFormField>

                <UFormField label="Prix" name="amount">
                    <UInput v-model="form.amount" class="w-full" type="number" />
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

                <UButton label="Freeze" type="submit"/>
            </UForm>
        </template>
    </UModal>
</template>