<script setup lang="ts">
import { ref, watchEffect } from "vue";
import { fetchListCategories, useFetchListCategories } from "../../composables/categories"
import { fetchListTags, useFetchListTags } from "../../composables/tags"
import { EditCategoryModal, EditTagModal } from "#components";

const {data: categories, error: errorCategories, refresh: refreshCategories} = useFetchListCategories()
const {data: tags, error: errorTags, refresh: refresTags }= useFetchListTags()

const overlay = useOverlay()
const modalCategory = overlay.create(EditCategoryModal, {
    props: {
        onSaved: async () => {
            refreshCategories()
        }
    }
})
const modalTag = overlay.create(EditTagModal, {
    props: {
        onSaved: async () => {
            refresTags()
        }
    }
})

const onModalCategory = (categoryId: string) => { 
    let cat = categories.value!.find(cat => cat.id === categoryId)
    if (cat)
        modalCategory.patch({ isEdit: true, categoryId: cat.id, title: cat.title, color: cat.color, icon: cat.icon})
    else 
        modalCategory.patch({isEdit: false, title: "", color: "", icon: ""})
    modalCategory.open()
}

const onModalTag = (tagId: string) => { 
    let tag = tags.value.find(tag => tag.id === tagId)
    if (tag)
        modalTag.patch({ isEdit:true, tagId: tag.id, value: tag.value, color: tag.color })
    else 
        modalTag.patch({ isEdit: false, value: '', color: ''})
    modalTag.open()
}

</script>

<template>
    <div>
        <div style="margin-top: 1rem;">
            <div class="flex justify-between items-center mb-3">
                <h3 class="font-semibold">Categories</h3>
                <UButton label="Ajouter categorie" icon="i-lucide-plus" @click="onModalCategory('')"/>
            </div>
            <USeparator class="mb-3"/>
            <div class="flex flex-wrap gap-3">
                <div v-for="category of categories" :key="category.id">
                    <div class="flex items-center gap-2 border-1 rounded-md p-1" :style="'color:'+category.color+';'">
                        <UIcon :name="category.icon" />
                        {{ category.title }}
                        <UButton variant="outline" color="neutral" icon="i-lucide-pencil" @click="onModalCategory(category.id)" />
                        <UButton  variant="outline" color="neutral" icon="i-lucide-trash-2" />
                    </div>
                </div>
            </div>
        </div>
        
        <div style="margin-top: 1rem;">
            <div class="flex justify-between items-center mb-3">
                <h3 class="font-semibold">Tags</h3>
                <UButton label="Ajouter tag" icon="i-lucide-plus" @click="onModalTag('')"/>
            </div>
            <USeparator class="mb-3" />
            <div class="flex flex-wrap gap-3">
                <div v-for="tag of tags" :key="tag.id">
                    <div class="flex items-center gap-2 border-1 rounded-md p-1" :style="'color:'+tag.color+';'">
                        {{ tag.value }}
                        <UButton variant="outline" color="neutral" icon="i-lucide-pencil" @click="onModalTag(tag.id)" />
                        <UButton  variant="outline" color="neutral" icon="i-lucide-trash-2" />
                    </div>
                </div>
            </div>
        </div>
        
    </div>    
</template>
