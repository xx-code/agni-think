<script lang="ts" setup>
import * as z from 'zod'
import { reactive } from "vue";
import { useFetchResumeAccount } from "../../composables/account";
import type { FormSubmitEvent } from '@nuxt/ui';

const props = defineProps({
    accountId: String
})

const schema = z.object({
    accountId: z.string().nonempty('Vous devez selection un compte'),
    amount: z.number().min(1, 'Vous de avoir un prix superieux a zero')
})

type Schema = z.output<typeof schema>

const accounts = useFetchResumeAccount()

const form = reactive({
    accountId: props.accountId ?? '',
    amount: 0
})

function onSubmit(event: FormSubmitEvent<Schema>) {
    console.log('submit')
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

                <UButton label="Freeze" type="submit"/>
            </UForm>
        </template>
    </UModal>
</template>