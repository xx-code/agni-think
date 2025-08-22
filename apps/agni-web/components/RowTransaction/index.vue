<script setup lang="ts">
import { UPopover } from '#components'

const props = defineProps<{
    id: string,
    balance: number,
    title: string,
    recordType: string,
    description: string,
    icon: string,
    color: string,
    tags: string[],
    date: Date,
    doShowEdit: boolean
}>();

const formatedDate = props.date.toLocaleString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
}) 

</script>

<template>
    <UPopover mode="hover" :content="{ align: 'end', side: 'top', sideOffset: 8}">
        <div class="row-transaction rounded-md border border-gray-400">
            <div class="flex justify-between items-center">
                <div class="col-span-2">
                    <div class="flex items-center gap-1">
                        <div class="icon" :style="{backgroundColor: `${color}22`}">
                            <UIcon :name="icon" :style="{color: color}" />
                        </div>
                        <div >
                            <div class="flex space-x-5">
                                <h3 class="font-semibold">{{ title }}</h3>
                                <p class="">{{ description }}</p>
                            </div>
                            <p class="text-xs text-gray-500">{{ formatedDate }}</p>
                        </div> 
                    </div>
                </div>
                <div class="justify-self-end">
                    <h3 class="font-semibold" :style="{color: recordType == 'Debit' ? '#1f2d38' : '#1abc9c'}">
                        {{ new Intl.NumberFormat('en-US', {style: 'currency',currency: 'CAD'}).format(balance) }}
                    </h3>
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
    background-color: white;
}
.icon {
    width: 35px;
    border-radius: 100%;
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