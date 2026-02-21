<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import type { EditFinancePrincipleType, FinancePrincipleType } from '~/types/ui/financePrinciple';
import { fetchPrincipleType } from '~/composables/internal';

const { financePrinciple } = defineProps<{
    financePrinciple?: FinancePrincipleType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditFinancePrincipleType, oldValue?: FinancePrincipleType): void    
    (e: 'close', close: boolean): void
}>();

const { data: principleTypes } = useAsyncData('principle-types', async () => {
    const res = await fetchPrincipleType()

    return res
})

const schema = z.object({
    name: z.string().nonempty("Name must not be empty"),
    description: z.string().nonempty("Descrpition must not be empty"),
    targetType: z.string().nonempty("Target type must not be empty"),
    strictness: z.number().min(1, "Strictness must be equale 1").max(10, "Strictness less or equale to 10"),
    logicRules: z.string().optional()
})

type Schema = z.output<typeof schema>

const form = reactive({
    name: financePrinciple?.name,
    description: financePrinciple?.description,
    targetType: financePrinciple?.targetType,
    strictness: financePrinciple?.strictness,
    logicRules: financePrinciple?.logicRules
});

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        name: data.name,
        description: data.description,
        targetType: data.targetType,
        strictness: data.strictness,
        logicRules: data.logicRules
    }, financePrinciple);

    form.name = ''
    form.description = ''
    form.targetType =''
    form.strictness = undefined
    form.logicRules = undefined

    emit('close', true)
}
</script>

<template>
    <UModal title="Edit Principe financier">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Name" name="name">
                    <UInput v-model="form.name" />
                </UFormField>

                <UFormField label="Description" name="description">
                    <UInput v-model="form.description" />
                </UFormField>

                <UFormField label="Rigueur" name="strictness">
                    <UInput 
                        type="number"
                        v-model="form.strictness"  />
                </UFormField>

                <UFormField>
                    <USelect 
                        v-if="principleTypes"
                        :items="principleTypes"
                        label-key="value"
                        value-key="id"
                        v-model="form.targetType" />
                </UFormField>

                <UFormField label="Regle logic pour agent(skill)" name="logicRules">
                    <UTextarea v-model="form.logicRules" />
                </UFormField>
               
                <UFormField>
                    <UButton label="Submit" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>