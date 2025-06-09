<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';
import { fetchCreateTag, fetchUpdateTag} from '../../composables/tags';

const props = defineProps({
    tagId: String,
    isEdit: Boolean,
    value: String,
    icon: String,
    color: String,
    onSaved: Function
})

const schema = z.object({
    value: z.string().nonempty('Vous devez ajouter une valeur'),
    color: z.string().nonempty('Vous devez ajouter une icon'),
})

type Schema = z.output<typeof schema>

const form = reactive({
    value: props.value ?? '',
    color: props.color ?? ''
})

const emit = defineEmits(['close'])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    if (!props.isEdit)
        await fetchCreateTag({value: form.value, color: form.color}) 
    else 
        await fetchUpdateTag({tagId: props.tagId ?? '', value: form.value, color: form.color})

    form.value = ''
    form.color = ''

    if (props.onSaved) props.onSaved()

    emit('close')
}
</script>

<template>
    <UModal title="Edit Tag">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class=" space-y-4">
                <UFormField label="Nom" name="value">
                    <UInput v-model="form.value" />
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
