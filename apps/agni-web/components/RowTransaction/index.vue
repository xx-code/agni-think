<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { library } from '@fortawesome/fontawesome-svg-core'
import {  useRoute } from 'nuxt/app'
import { computed, watch } from 'vue'
import { UPopover } from '#components'


library.add(fas)

const props = defineProps({
    id: String,
    balance: Number,
    title: String,
    description: String,
    icon: String,
    tags: Array,
    doShowEdit: Boolean
})
</script>

<template>
    <UPopover mode="hover" :content="{ align: 'end', side: 'top', sideOffset: 8}">
        <div class="row-transaction rounded-md">
            <div class="flex justify-between items-center">
                <div class="col-span-2">
                    <div class="flex items-center gap-1">
                        <div class="icon rounded-md">
                            <font-awesome-icon :icon="icon" />
                        </div>
                        <div >
                            <h3 class="font-semibold">{{ title }}</h3>
                            <div class="flex items-center">
                                <p class="text-xs antialiased" style="color:#ADADAD">{{ description }}</p>
                                <div class="tags gap-1 ml-1">
                                    <div v-for="tag in tags" :key="tag">
                                        <UBadge class="rounded-full" size="xs" >
                                            {{ tag }}
                                        </UBadge>
                                    </div>
                                </div>
                            </div>
                            
                        </div> 
                    </div>
                </div>
                <div class="justify-self-end">
                    <h3 class="font-semibold">${{ balance }}</h3>
                </div>
                
                
            </div> 
        </div>

        <template v-if="doShowEdit" #content>
            <div  class="flex justify-self-end">
                <div class="flex gap-2">
                    <UButton icon="i-lucide-pencil" color="neutral" variant="subtle" @click="$emit('update', id)"/>
                    <UButton icon="i-lucide-trash" color="error" @click="$emit('delete', id)"/>
                </div>
            </div>
        </template>

    </UPopover>
</template>

<style scoped lang="scss">
.row-transaction {
    padding: 0.5rem;
    background-color: rgba(103, 85, 215, 0.1);
}
.icon {
    background: #fff;
    color: #1E3050;
    width: 35px;
    height: 35px;
    display: flex;
    justify-content: center;
    align-items: center;
}
.tags {
    display: flex;
    flex-wrap: wrap; 
    align-items: center;
}
</style>