<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import { useFetchResumeAccount } from "../../composables/account";
import type { FormSubmitEvent } from '@nuxt/ui';
import { fetchCreateGoal, fetchUpdateGoal, useFetchGoal } from '../../composables/goals';

const props = defineProps({
    goalId: String,
    isEdit: Boolean,
    onSaved: Function
})

const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    targetAmount: z.number().min(1, 'La somme d\'argent doit etre superieux a zero')
})

type Schema = z.output<typeof schema>

let saveGoal = null
if (props.goalId)
    saveGoal = await useFetchGoal(props.goalId)

const form = reactive({
    title: saveGoal ? saveGoal.value?.title ?? '' : '',
    description: saveGoal ? saveGoal.value?.desciprtion ?? '' : '',
    targetAmount: saveGoal ? saveGoal.value?.targetAmount ?? 0 : 0
})

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit)
        await fetchCreateGoal({title: form.title, description: form.description, target: form.targetAmount})
    else
        await fetchUpdateGoal({saveGoalId: props.goalId!, title: form.title, description: form.description, target: form.targetAmount})
    
    if (props.onSaved) props.onSaved()

    emit('close')
}

</script>

<template>
    <UModal title="Etiteur de but d'epargne" >
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Nom du but d'epargne" name="title">
                    <UInput v-model="form.title" class="w-full" />
                </UFormField>
                <UFormField label="Petit description d'epargne" name="description">
                    <UInput v-model="form.description" class="w-full" />
                </UFormField>
                <UFormField label="Someme du but d'epargne" name="targetAmount">
                    <UInput v-model="form.targetAmount" class="w-full" type="number" />
                </UFormField>
                <UButton label="Submit" type="submit" />
            </UForm>
        </template>
    </UModal>
</template>