<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import { fetchCreateCategory, fetchUpdateCategory} from '../../composables/categories';

const props = defineProps({
    isEdit: Boolean,
    categoryId: String,
    title: String,
    icon: String,
    color: String,
    onSaved: Function
})

const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    icon: z.string().nonempty('Vous devez ajouter une icon'),
})

type Schema = z.output<typeof schema>

const form = reactive({
    title: props.title ?? '',
    icon: props.icon ?? '',
    color: props.color ?? ''
})

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit) {
        await fetchCreateCategory({title: form.title, icon: form.icon, color: form.color})
    } else {
        await fetchUpdateCategory({categoryId: props.categoryId??'', title: form.title, color: form.color, icon: form.icon})
    }

    if (props.onSaved) props.onSaved()

    emit('close')
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