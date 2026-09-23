<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui';
import { color } from 'chart.js/helpers';
import { TRANSFER_CATEGORY_ID } from '~/shared/constantBackend';
import type { SlideQuickViewTransactionType } from '~/types/ui/account';

const { invoices, hasMore, loading } = defineProps<{
    invoices: SlideQuickViewTransactionType[],
    hasMore: boolean
    loading: boolean
}>();

const emit = defineEmits<{
    update: [id: string]
    delete: [id: string]
    valid: [id: string]
    cancelTransfer: [id: string]
}>()

function getDropdownItems(invoice: SlideQuickViewTransactionType): DropdownMenuItem[][] {
    const editActions = 
        {
            class: 'items-center',
            label: 'Modifier',
            icon: 'i-lucide-square-pen',
            onSelect: () => emit('update', invoice.id)
        }
    const completeAction =
        {
            class: 'items-center',
            label: 'Valider',
            icon: 'i-lucide-check',
            onSelect: () => emit('valid', invoice.id)
        }

    const cancelActions= {
            class: 'items-center',
            label: 'Annuler transfert',
            icon: 'i-lucide-trash',
            onSelect: () => emit('cancelTransfer', invoice.id)
    }
    const deletAction = {
            class: 'items-center',
            label: 'Supprimer',
            icon: 'i-lucide-trash',
            color: 'error',
            onSelect: () => emit('delete', invoice.id)
        }
    
    const listEdit = [editActions]
    const listRemove = [deletAction]

    // if (invoice.isFreeze) {
    //     listEdit.splice(0, 1)
    // }

    if (invoice.status.toLowerCase() === 'pending') {
        listEdit.push(completeAction)
    }

    if (invoice.category === 'Transfert')
        return [ [ cancelActions ] ]

    return [
        listEdit,
        listRemove
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
                    <UDropdownMenu :ui=" {item: 'items-center font-semibold'}" :items="getDropdownItems(invoice)">
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


