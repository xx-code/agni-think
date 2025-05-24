<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import { reactive } from 'vue';
import { addNewAccount, updateAccount, useFetchAccountTypes } from '../../composables/account';

const props = defineProps({
    isEdit: Boolean,
    accountId: String,
    accountName: String,
    accountType: String
})


const types = useFetchAccountTypes()

const schema = z.object({
    accountName: z.string().nonempty('Le nom du compte est vide'),
    accountType: z.string().nonempty('Vous devez selection un type de compte')
})

type Schema = z.output<typeof schema>

const form = reactive({
    accountName: props.accountName ?? '',
    accountType: props.accountType ?? (types.value[0].id ?? '')
})

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit)
        addNewAccount({accountName: form.accountName, accountType: form.accountType})
    else 
        updateAccount({accountId: props.accountId??'', accountName: form.accountName, accountType: form.accountType})
    form.accountName = ""
    form.accountType = ""
}

</script>

<template>
   <UModal 
        :close="{ onClick: () => $emit('close', false)}"
        title="Editeur de compte">
    <template #body>
        <UForm :schema="schema" :state="form" class="space-y-4" @submit="onSubmit">
            <UFormField label="Nom de compte" name="accountName">
                <UInput v-model="form.accountName"/>
            </UFormField>

            <UFormField label="Type de compte" name="accountType">
                <USelect v-model="form.accountType" value-key="id" :items="types">

                </USelect>
            </UFormField>
            <UButton type="submit" :label="isEdit ? 'Ajouter nouveau' : 'Mettre a jour'"/>
        </UForm>
    </template>
   </UModal> 
</template>

<style scoped>

</style>