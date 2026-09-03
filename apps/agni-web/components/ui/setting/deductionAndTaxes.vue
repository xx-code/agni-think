<script setup lang="ts">
import { ModalEditDeductionType } from '#components';
import { deductionResponseToDeduction, listDeductionsResponseToListDeductions } from '~/mappers/deduction';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest, ListResponse } from '~/types/api';
import type { GetDeductionResponse } from '~/types/api/deduction';
import type { DeductionType, EditDeduction } from '~/types/ui/deduction';


const overlay = useOverlay()
const toast = useToast()
const modalEditDeductionType = overlay.create(ModalEditDeductionType);
const { data: deductionTypes, error: errorDeductionType, refresh: refreshDeductionTypes } = useAsyncData('settings+deduction-types', async () => {
    const res = await ApiLinkBuilder.route<ListResponse<GetDeductionResponse>>(API_ROUTES.DEDUCTIONS.GET_DEDUCTIONS).query({ queryAll: true, limit: 0, offset: 0}).mapper(listDeductionsResponseToListDeductions).execute()

    return res.items
})

async function onSubmitDeductionType(value: EditDeduction, oldValue?: DeductionType) {
    try {
        if(oldValue) {
            await ApiLinkBuilder.route(API_ROUTES.DEDUCTIONS.UPDATE_DEDUCTION).params({id: oldValue.id}).body({
                description: value.description,
                title: value.title
            }).execute();
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.DEDUCTIONS.CREATE_DEDUCTION).body({
                title: value.title,
                description: value.description,
                mode: value.mode,
                base: value.base
            }).execute();
        }
        await refreshDeductionTypes();
    } catch(err) {
        toast.add({
            title: "Error Deduction type",
            description:`Error while submit deduction type`, 
            color: 'error'
        });
    }
}

const openModalDeductionType = async (id?: string) => {  
    let type:DeductionType|undefined=undefined;
    if (id) {
        type = await ApiLinkBuilder.route<GetDeductionResponse>(API_ROUTES.DEDUCTIONS.GET_DEDUCTION).params({id}).mapper(deductionResponseToDeduction).execute(); 
    }

    modalEditDeductionType.open({
        deductionType: type, 
        onSubmit: onSubmitDeductionType
    });
}

const onDeleteDeductionType = async (id: string) => {
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
}

</script>

<template>
    <UCard>
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
                    @click="openModalDeductionType()"
                />
            </div>

            <div class="max-w-md">
                <UInput 
                    icon="i-lucide-search" 
                    placeholder="Rechercher une déduction..." 
                    size="md"
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
                                        @click="openModalDeductionType(deduction.id)" 
                                    />
                                    <UButton 
                                        variant="ghost" 
                                        color="error" 
                                        icon="i-lucide-trash-2" 
                                        size="xs" 
                                        @click="onDeleteDeductionType(deduction.id)"
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
                    @click="openModalDeductionType()"
                />
            </div>
        </div>
    </UCard>

</template>