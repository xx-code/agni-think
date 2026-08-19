<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import type { SlideQuickViewTransactionType } from '~/types/ui/account';

const { invoices, hasMore, loading } = defineProps<{
    invoices: SlideQuickViewTransactionType[],
    hasMore: boolean
    loading: boolean
}>();

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
}>()

function getDropdownItems(invoiceId: string): DropdownMenuItem[][] {
    return [
        [
            {
                label: 'Modifier',
                icon: 'i-lucide-square-pen',
                onSelect: () => emit('update', invoiceId)
            },
        ],
        [
            {
                label: 'Supprimer',
                icon: 'i-lucide-trash',
                color: 'error',
                onSelect: () => emit('delete', invoiceId)
            }
        ]
    ]
}


function getTypeColor(type: string) {
    if (type.toLowerCase() === 'income')
        return '#10b981'
    else if (type.toLowerCase() === 'other')
        return '#b2bac4'
    else
        return '#ef4444'
}


</script>

<template>
    <div class="relative space-y-6">
        <div class="w-px h-full bg-neutral-200 absolute left-1.5 -z-10"></div>
        <div class="flex items-center gap-5" 
            v-for="invoice in invoices" 
            :key="invoice.id">
            <div class="w-3 h-3 rounded-2xl" :style="{ backgroundColor: invoice.color }"/>    
            <UIcon :name="invoice.icon" />
            
            <div class="flex flex-1 items-center justify-between">
                <div class="">
                    <p class="font-semibold">{{ invoice.description }}</p>
                    <p class="text-xs">{{ formatDate(invoice.date)  }} · {{ invoice.status }}</p>
                </div>

                <div class="flex items-center">
                    <p class="font-semibold mr-1" :style="{ color: getTypeColor(invoice.type)}">{{ formatCurrency(invoice.total) }}</p>
                    <UDropdownMenu :ui=" {item: 'items-center font-semibold'}" :items="getDropdownItems(invoice.id)">
                        <UButton 
                            size="xs"
                            icon="i-lucide-ellipsis"
                            variant="ghost"
                        />
                    </UDropdownMenu> 
                </div>
            </div> 
        </div>

        <!-- Infinite Scroll Sentinel & Status indicators -->
        <div class="py-4 text-center">
            <p v-if="loading" class="text-gray-500">Chargement des articles...</p>
            <p v-else-if="!hasMore" class="text-gray-400 text-sm">No more items to display</p>
        </div>
    </div> 
</template>


