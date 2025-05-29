<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import { useFetchResumeAccount } from "../../composables/account";
import type { FormSubmitEvent } from '@nuxt/ui';

const props = defineProps({
    goalId: String,
    title: String,
    targetAmount: Number
})

const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    targetAmount: z.number().min(1, 'La somme d\'argent doit etre superieux a zero')
})

type Schema = z.output<typeof schema>

const form = reactive({
    title: props.title,
    targetAmount: props.targetAmount
})

function onSubmit(event: FormSubmitEvent<Schema>) {
    console.log("submit")
}

</script>

<template>
    <UModal title="Etiteur de but d'epargne" >
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Nom du but d'epargne">
                    <UInput v-model="form.title" class="w-full" />
                </UFormField>
                <UFormField label="Someme du but d'epargne">
                    <UInput v-model="form.targetAmount" class="w-full" type="number" />
                </UFormField>
                <UButton label="Submit" type="submit" />
            </UForm>
        </template>
    </UModal>
</template>