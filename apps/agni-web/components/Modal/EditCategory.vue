<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { CategoryType, EditCategoryType } from '~/types/ui/category';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest } from '~/types/api';

const { category } = defineProps<{
    category?: CategoryType
}>();
const emit = defineEmits<{
    (e: 'close', doRefresh: boolean): void
}>();

const toast = useToast()

const form = reactive<Partial<EditCategoryType>>({
    title: category?.title,
    icon: category?.icon,
    color: category?.color
})

function validate(data: Partial<EditCategoryType>): FormError[] {
    const errors: FormError[] = []

    if (!data.title) errors.push({ name: 'title', message:  'Vous devez ajouter un titre'})
    if (!data.icon) errors.push({name: 'icon', message: 'Vous devez ajouter une icon'})

    return errors
}

async function onSubmit(event: FormSubmitEvent<EditCategoryType>) {
    const data = event.data
    try {
        if(category) {
            await ApiLinkBuilder.route(API_ROUTES.CATEGORIES.UPDATE_CATEGORY).params({id: category.id}).body({
                title: data.title,
                icon: data.icon,
                color: data.color
            }).execute();
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.CATEGORIES.CREATE_CATEGORY).body({
                title: data.title,
                icon: data.icon,
                color: data.color
            }).execute();
        }
        emit('close', true)
    } catch(err) {
        toast.add({
            title: "Error Category",
            description:`Error while submit category`, 
            color: 'error'
        });
    }

    Object.assign(form, {
        title: undefined,
        color: undefined,
        icon: undefined
    })
}
</script>

<template>
    <UModal title="Edit Categorie">
        <template #body>
            <UForm :validate="validate" :state="form" @submit="onSubmit" class=" space-y-4">
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