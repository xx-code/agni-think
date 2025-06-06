<script setup lang="ts">
import { ref } from "vue";
import { EditCategoryModal, EditTagModal } from "#components"
import { fetchListCategories, useFetchListCategories } from "../../composables/categories"
import { useFetchTags } from "../../composables/tags"


const categories = await useFetchListCategories()
const tags = useFetchTags()

const overlay = useOverlay()
const modalCategory = overlay.create(EditCategoryModal, {
    props: {
        onSaved: async () => {
            categories.value = await fetchListCategories()
        }
    }
})
const modalTag = overlay.create(EditTagModal, {
    props: {}
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
        modalTag.patch({ tagId: tag.id, value: tag.value, color: tag.color })
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
            <div class="flex gap-1">
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
            <div class="flex gap-1">
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
