<script setup lang="ts">
import * as z from 'zod';
import type { FormSubmitEvent } from '@nuxt/ui';
import { reactive } from 'vue';
import { useFetchListAccountTypes, fetchUpdateAccount, fetchCreateAccount } from '../../composables/account';

const props = defineProps({
    isEdit: Boolean,
    accountId: String,
    accountName: String,
    accountType: String,
    onSaved: Function
})

const types = await useFetchListAccountTypes()

const schema = z.object({
    accountName: z.string().nonempty('Le nom du compte est vide'),
    accountType: z.string().nonempty('Vous devez selection un type de compte')
})

type Schema = z.output<typeof schema>

const form = reactive({
    accountName: props.accountName ?? '',
    accountType: props.accountType ?? (types.value[0].id ?? '')
})

const  emit = defineEmits(['close', 'saved'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit) {
        await fetchCreateAccount({accountName: form.accountName, accountType: form.accountType})
    } else {
        await fetchUpdateAccount({accountId: props.accountId??'', accountName: form.accountName, accountType: form.accountType})
    }
        
    form.accountName = ""
    form.accountType = ""

    if (props.onSaved) props.onSaved()
    emit('close')
}

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
                <USelect v-model="form.accountType" value-key="id" :items="types" class="w-full">

                </USelect>
            </UFormField>
            <UButton type="submit" :label="isEdit ? 'Mettre a jour' : 'Ajouter nouveau'"/>
        </UForm>
    </template>
   </UModal> 
</template>

<style scoped>

</style>