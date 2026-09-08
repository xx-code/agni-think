<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { EditTagType, TagType } from '~/types/ui/tag';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest } from '~/types/api';

const { tag } = defineProps<{
    tag?: TagType
}>();
const emit = defineEmits<{
    (e: 'close', refresh: boolean): void
}>();

const toast = useToast()


function validate(data: Partial<EditTagType>): FormError[] {
    const errors: FormError[] = []

    if (!data.value) errors.push({ name: '', message: 'Vous devez ajouter une valeur'})
    if (!data.color) errors.push({ name: '', message: 'Vous devez ajouter une color'})

    return errors
}

const form = reactive<Partial<EditTagType>>({
    value: tag?.value,
    color: tag?.color
});

async function onSubmit(event: FormSubmitEvent<EditTagType>) {
    const data = event.data;
    try {
        if(tag) {
            await ApiLinkBuilder.route(API_ROUTES.TAGS.UPDATE_TAG).params({id: tag.id}).body({
                value: data.value,
                color: data.color
            }).execute();
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.TAGS.CREATE_TAG).body({
                value: data.value,
                color: data.color
            }).execute();
        }
    } catch(err) {
        toast.add({
            title: "Error tag",
            description:`Error while submit tag`, 
            color: 'error'
        });
    }

    form.value = ''
    form.color = ''
    emit('close', true);
}
</script>

<template>
    <UModal title="Edit Tag">
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class=" space-y-4">
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
