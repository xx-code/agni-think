<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import useAccountTypes from '~/composables/internals/useAccountTypes';
import type { AccountType, EditAccountType } from '~/types/ui/account';

const { account } = defineProps<{
    account?: AccountType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditAccountType, oldValue?: AccountType): void    
    (e: 'close', close: boolean): void
}>(); 

const {data: types} = useAccountTypes();

const schema = z.object({
    accountName: z.string().nonempty('Le nom du compte est vide'),
    accountType: z.string().nonempty('Vous devez selection un type de compte')
})

type Schema = z.output<typeof schema>

const form = reactive({
    accountName: account?.title || '',
    accountType: account?.type || ''
})


async function onSubmit(event: FormSubmitEvent<Schema>) {
    emit('submit', {
        title: form.accountName,
        type: form.accountType 
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
            <UButton type="submit" :label="account ? 'Mettre a jour' : 'Ajouter nouveau'"/>
        </UForm>
    </template>
   </UModal> 
</template>