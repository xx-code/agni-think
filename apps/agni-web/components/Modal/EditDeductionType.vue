<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '#ui/types';
import type { DeductionTypeType, EditDeductionType } from '~/types/ui/deduction';

const { deductionType } = defineProps<{
    deductionType?: DeductionTypeType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditDeductionType, oldValue?: DeductionTypeType): void    
    (e: 'close', close: boolean): void
}>();

const form = reactive<Partial<EditDeductionType>>({
    title: deductionType?.title || undefined,
    description: deductionType?.description || undefined,
    base: deductionType?.base || undefined,
    mode: deductionType?.mode || undefined
})

function validation(state: Partial<EditDeductionType>): FormError[]  {
    const errors: FormError[] = []
    if (!state.title)
        errors.push({ name: 'title', message: 'Vous devez mettre un titre'})

    if (!state.base)
        errors.push({ name: 'base', message: 'Vous devez selectionn une base'})
    
    if (!state.mode)
        errors.push({ name: 'mode', message: 'Vous devez selectionner un mode'})


    return errors
}

async function onSubmit(event: FormSubmitEvent<EditDeductionType>) {
    const data = event.data;

    emit('submit', { 
        title: data.title,
        description: data.description,
        base: data.base, 
        mode: data.mode
    }, deductionType);
    
    emit('close', false);
}

</script>

<template>
    <UModal title="Edit DeductionType">
        <template #body>
            <UForm :state="form" :validate="validation" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Nom" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Description" name="description">
                    <UInput v-model="form.description" />
                </UFormField>

                <UFormField label="Base" name="base">
                    <USelectMenu 
                        :disabled="deductionType !== undefined"
                        :items="[{label: 'SubTotal', value: 'Subtotal'}, {label: 'Total', value: 'Total'}]"
                        class="w-full"
                        value-key="value"
                        v-model="form.base"
                    />
                </UFormField>

                <UFormField label="Mode" name="mode">
                    <USelectMenu 
                        :disabled="deductionType !== undefined"
                        :items="[{label: 'Flat', value: 'Flat'}, {label: 'Rate', value: 'Rate'}]"
                        class="w-full"
                        value-key="value"
                        v-model="form.mode"
                    />
                </UFormField>

                
                <UFormField>
                    <UButton label="Submit" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>