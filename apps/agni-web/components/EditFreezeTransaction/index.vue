<script lang="ts" setup>
import * as z from 'zod'
import { reactive, shallowRef } from "vue";
import { useFetchResumeAccount } from "../../composables/account";
import type { FormSubmitEvent } from '@nuxt/ui';
import { fetchFreezeTransaction } from '../../composables/transactions';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'

const props = defineProps({
    accountId: String,
    onSaved: Function
})

const schema = z.object({
    accountId: z.string().nonempty('Vous devez selection un compte'),
    amount: z.number().min(1, 'Vous de avoir un prix superieux a zero')
})

type Schema = z.output<typeof schema>

const accounts = await useFetchResumeAccount()

const form = reactive({
    accountId: props.accountId ?? '',
    amount: 0
})

const date = shallowRef(new CalendarDate(new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()))
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    await fetchFreezeTransaction({accountId: form.accountId, amount: form.amount, endDate: date.value.toString()})   
    if(props.onSaved) props.onSaved()
    emit('close')
}

</script>

<template>
    <UModal title="Freeze de transaction">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Compte" name="accountId">
                    <USelect v-model="form.accountId" value-key="id" label-key="title" :items="accounts.filter(acc => acc.id !== ALL_ACCOUNT_ID)" class="w-full"/>
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