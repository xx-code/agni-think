<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { EditFinancePrincipleType, FinancePrincipleType } from '~/types/ui/financePrinciple';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest } from '~/types/api';

const { financePrinciple } = defineProps<{
    financePrinciple?: FinancePrincipleType
}>();

const emit = defineEmits<{
    (e: 'close', refresh: boolean): void
}>();

const toast = useToast()

const { data: principleTypes } = useAsyncData('principle-types', async () => {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PRINCIPLE_TYPE)
        .execute()
})

function validate(data: Partial<EditFinancePrincipleType>): FormError[] {
    const errors: FormError[] = []

    if (!data.name) errors.push({ name: 'name', message: 'les noms sont vide'})
    if (!data.description) errors.push({ name: 'description', message: 'description ne doit pas etre vide'})
    if (!data.targetType) errors.push({ name: 'targetType', message: 'type target ne doit pas etre vide'})
    if (!data.strictness || (data.strictness <= 1 && data.strictness >= 10)) errors.push({ name: 'strictness', message: 'Strictness doit etre entre 1 et 10'})

    return errors
}


const form = reactive<Partial<EditFinancePrincipleType>>({
    name: financePrinciple?.name,
    description: financePrinciple?.description,
    targetType: financePrinciple?.targetType,
    strictness: financePrinciple?.strictness,
    logicRules: financePrinciple?.logicRules
});

async function onSubmit(event: FormSubmitEvent<FinancePrincipleType>) {
    const data = event.data;
    try {
        if (financePrinciple) {
            await ApiLinkBuilder.route(API_ROUTES.FINANCE_PRINCIPLES.UPDATE_FINANCE_PRINCIPLE).params({id: financePrinciple.id}).body({
                name: data.name,
                strictness: data.strictness,
                targetType: data.targetType,
                description: data.description,             
                logicRules: data.logicRules
            }).execute()
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.FINANCE_PRINCIPLES.CREATE_FINANCE_PRINCIPLE).body({
                name: data.name,
                strictness: data.strictness,
                targetType: data.targetType,
                description: data.description,             
                logicRules: data.logicRules
            }).execute()
        }

    } catch(err: any) {
        toast.add({
            title: "Error finance principle",
            description: err?.message, 
            color: 'error'
        });
    }
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
            <UForm :valide="validate" :state="form" @submit="onSubmit" class=" space-y-4">
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