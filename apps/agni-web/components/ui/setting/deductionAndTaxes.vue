<script setup lang="ts">
import { useDeductionModal } from '~/composables/modal/deduction';
import useConfirmModal from '~/composables/modal/useConfirmModal';
import { listDeductionsResponseToListDeductions } from '~/mappers/deduction';
import { API_ROUTES } from '~/shared/routes';
import type { ListResponse } from '~/types/api';
import type { GetDeductionResponse } from '~/types/api/deduction';

const overlay = useOverlay()
const toast = useToast()
const { open } = useDeductionModal(overlay)
const { open: openConfirm } = useConfirmModal(overlay)
const { data: deductionTypes, refresh: refreshDeductionTypes } = useAsyncData('settings+deduction-types', async () => {
    const res = await ApiLinkBuilder.route<ListResponse<GetDeductionResponse>>(API_ROUTES.DEDUCTIONS.GET_DEDUCTIONS).query({ queryAll: true, limit: 0, offset: 0}).mapper(listDeductionsResponseToListDeductions).execute()

    return res.items
})

const onDeleteDeductionType = async (id: string, title: string) => {
    openConfirm({
        title: `Voulez vous supprimer ${title}?`,
        description: ''
    }, async () => {
        try {
            await ApiLinkBuilder.route(API_ROUTES.DEDUCTIONS.DELETE_DEDUCTION).params({id}).execute()
            refreshDeductionTypes()
        } catch(err) {
            toast.add({
                title: "Error delete deduction type",
                description:`Error while delete deduction type`, 
                color: 'error'
            });       
        }
    }) 
}

</script>

<template>
    <UiCard>
        <div class="space-y-6">
            <div class="flex items-end justify-between">
                <div>
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Déductions & Taxes</h2>
                    <p class="text-sm text-gray-500">Gestion des déductions actives et de leurs taux</p>
                </div>
                <UButton 
                    label="Nouvelle déduction" 
                    icon="i-lucide-plus" 
                    size="md"
                    color="primary"
                    @click="open(refreshDeductionTypes)"
                />
            </div>

            <!-- Table view for deductions -->
            <div class="overflow-hidden border border-gray-200 rounded-xl">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Titre
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Description
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Base
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Mode
                            </th>
                            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Actions
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <tr v-for="deduction of deductionTypes" 
                            :key="deduction.id"
                            class="hover:bg-gray-50 transition-colors">
                            <td class="px-6 py-4 whitespace-nowrap">
                                <div class="flex items-center gap-3">
                                    <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-purple-50">
                                        <UIcon 
                                            name="i-lucide-percent" 
                                            class="w-4 h-4 text-purple-600"
                                        />
                                    </div>
                                    <span class="text-sm font-medium text-gray-900">
                                        {{ deduction.title }}
                                    </span>
                                </div>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                    {{ deduction.description }}
                                </span>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                    {{ deduction.base }}
                                </span>
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                    {{ deduction.mode }}
                                </span>
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-right">
                                <div class="flex items-center justify-end gap-2">
                                    <UButton 
                                        variant="ghost" 
                                        color="neutral" 
                                        icon="i-lucide-pencil" 
                                        size="xs"
                                        @click="open(refreshDeductionTypes, deduction.id)" 
                                    />
                                    <UButton 
                                        variant="ghost" 
                                        color="error" 
                                        icon="i-lucide-trash-2" 
                                        size="xs" 
                                        @click="onDeleteDeductionType(deduction.id, deduction.title)"
                                    />
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Empty state -->
            <div v-if="!deductionTypes || deductionTypes.length === 0" 
                class="text-center py-12 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
                <UIcon name="i-lucide-percent" class="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p class="text-gray-500 text-sm">Aucune déduction configurée</p>
                <UButton 
                    label="Créer la première déduction" 
                    size="sm" 
                    class="mt-3"
                    @click="open(refreshDeductionTypes)"
                />
            </div>
        </div>
    </UiCard>

</template>