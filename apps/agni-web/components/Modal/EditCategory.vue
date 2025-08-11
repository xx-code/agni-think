<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import type { CategoryType, EditCategoryType } from '~/types/ui/category';

const { category } = defineProps<{
    category?: CategoryType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditCategoryType, oldValue?: CategoryType): void    
    (e: 'close', close: boolean): void
}>();

const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    icon: z.string().nonempty('Vous devez ajouter une icon'),
})

type Schema = z.output<typeof schema>

const form = reactive({
    title: category?.title || '',
    icon: category?.icon || '',
    color: category?.color || ''
});

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', {
        title: data.title,
        icon: data.icon,
        color: form.color 
    }, category);

    form.title = ''
    form.color = ''
    form.icon =''

    emit('close', true)
}
</script>

<template>
    <UModal title="Edit Categorie">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Nom" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Icon" name="icon">
                    <UInput v-model="form.icon" />
                </UFormField>

                <UFormField label="Couleur" name="color">
                    <div class="flex items-center gap-1">
                        <div class="rounded-md" :style="'width: 25px; height: 25px;'+'background-color:'+form.color+';'"></div>
                        <UPopover>
                            <UButton color="neutral" variant="subtle" icon="i-lucide-palette" >Color Picker</UButton>
                            <template #content>
                                <UColorPicker v-model="form.color" />
                            </template>
                        </UPopover>
                    </div>
                </UFormField>
                
                <UFormField>
                    <UButton label="Submit" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal> 
</template>