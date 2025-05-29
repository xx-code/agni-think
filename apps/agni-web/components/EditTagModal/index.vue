<script setup lang="ts">
import * as z from 'zod'
import { reactive } from "vue";
import type { FormSubmitEvent } from '@nuxt/ui';

const props = defineProps({
    tagId: String,
    value: String,
    icon: String,
    color: String
})

const schema = z.object({
    value: z.string().nonempty('Vous devez ajouter une valeur'),
    icon: z.string().nonempty('Vous devez ajouter une icon'),
})

type Schema = z.output<typeof schema>

const form = reactive({
    tagId: props.tagId,
    value: props.value,
    color: props.color
})

function onSubmit(event: FormSubmitEvent<Schema>) {
    console.log('submit')
}
</script>

<template>
    <UModal title="Edit Categorie">
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
