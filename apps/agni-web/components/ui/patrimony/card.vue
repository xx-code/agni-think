<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import { getLabelPatrimonyType } from '~/types/constants/patrimony';
import type { PatrimonyCard } from '~/types/ui/patrimony';

const { patrimony } = defineProps<{
    patrimony: PatrimonyCard 
}>()

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
}>()

const getIndicator = computed(() => {
    const evolution = roundNumber(patrimony.evolution)
    if (evolution === 0)
        return { textColor: 'text-gray-400', icon: 'i-lucide-minus' }

    let isUp = evolution > 0
    if (patrimony.type === 'Liability')
        isUp = evolution < 0
    
    return isUp ? { textColor: 'text-green-500', icon: 'i-lucide-trending-up' } : { textColor: 'text-red-500', icon: 'i-lucide-trending-down'}
})

const actionItems = ref<DropdownMenuItem[][]>([
    [
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', patrimony.id)
        },
    ],
    [
        {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', patrimony.id)
        }
    ]
])
</script>

<template>
    <div class="bg-white p-5 rounded-2xl shadow-sm cursor-pointer">
        <div class="flex justify-between items-start">
            <span 
                :class="[
                    'px-2.5 py-0.5 font-medium text-[0.70rem]  rounded-full',
                    patrimony.type === 'Asset' ? 'bg-green-500/10 text-green-700' : 'bg-red-500/10 text-red-700'
                ]" 
            >
            {{ getLabelPatrimonyType(patrimony.type).toUpperCase()  }}
            </span>
            <UDropdownMenu :items="actionItems" >
                <UButton 
                    @click.stop
                    icon="i-lucide-ellipsis-vertical"
                    variant="ghost"
                    color="neutral"
                />
            </UDropdownMenu> 
        </div>

        <div class="mb-4">
            <h1 class="font-semibold truncate" :title="patrimony.title">{{ patrimony.title }}</h1>
            <p class="text-sm text-gray-500 truncate" :title="patrimony.description">{{ patrimony.description }}</p>
        </div>

        <div class="flex justify-between items-center">
            <span class="text-lg font-bold">{{ formatCurrency(patrimony.balance) }}</span>
            <div class="flex items-center text-sm font-bold" :class="getIndicator.textColor">
                <UIcon class="mr-1" :name="getIndicator.icon" />
                <span>
                    {{ roundNumber(patrimony.evolution)  }}
                </span>
                <span>%</span>
            </div>
        </div>

    </div>
</template>