<script setup lang="ts">
import type { NuxtError } from '#app';
import { ModalEditFinancePrinciple } from '#components';
import { listFinancePrinciplesResponseToListFinancePrinciples, financePrincipleResponseToFinancePrinciple } from '~/mappers/financePrinciple';
import { API_ROUTES } from '~/shared/routes';
import type { CreatedRequest, ListResponse } from '~/types/api';
import type { GetFinancePrincipleResponse } from '~/types/api/financePrinciple';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import type { FinancePrincipleType, EditFinancePrincipleType } from '~/types/ui/financePrinciple';

const toast = useToast()
const overlay = useOverlay()
const modalEditFinancePrinciple = overlay.create(ModalEditFinancePrinciple)

const { data, error: errorFinancePrinciples, refresh: refreshPrinciples } = useAsyncData('finance-principles+all+setting', async () => {

    const [ principleTypes, financePrinciples ] = await Promise.all([
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PRINCIPLE_TYPE).execute(),
        ApiLinkBuilder.route<ListResponse<GetFinancePrincipleResponse>>(API_ROUTES.FINANCE_PRINCIPLES.GET_FINANCE_PRINCIPLES)
            .query({ queryAll: true, limit: 0, offset: 0})
            .mapper(listFinancePrinciplesResponseToListFinancePrinciples).execute()
    ])

    return {
        principleTypes,
        financePrinciples: financePrinciples.items
    }
})

const openModalFinancePrinciple = async (id?: string) => {  
    let principe:FinancePrincipleType|undefined=undefined;
    if (id) {
        principe = await ApiLinkBuilder.route<GetFinancePrincipleResponse>(API_ROUTES.FINANCE_PRINCIPLES.GET_FINANCE_PRINCIPLE).params({id}).mapper(financePrincipleResponseToFinancePrinciple).execute(); 
    }

    modalEditFinancePrinciple.open({
        financePrinciple: principe,
        onSubmit: onSubmitFinancePrinciple
    });
}


async function onSubmitFinancePrinciple(value: EditFinancePrincipleType, oldValue?: FinancePrincipleType) {
    try {
        if (oldValue) {
            await ApiLinkBuilder.route(API_ROUTES.FINANCE_PRINCIPLES.UPDATE_FINANCE_PRINCIPLE).params({id: oldValue.id}).body({
                name: value.name,
                strictness: value.strictness,
                targetType: value.targetType,
                description: value.description,             
                logicRules: value.logicRules
            }).execute()
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.FINANCE_PRINCIPLES.CREATE_FINANCE_PRINCIPLE).body({
                name: value.name,
                strictness: value.strictness,
                targetType: value.targetType,
                description: value.description,             
                logicRules: value.logicRules
            }).execute()
        }

        await refreshPrinciples()
    } catch(err) {
        const nuxtError = err as NuxtError
        toast.add({
            title: "Error finance principle",
            // description: nuxtError.data, 
            color: 'error'
        });
    }
}

const onDeletePrinciple = async (id: string) => {
    try {
        await ApiLinkBuilder.route(API_ROUTES.FINANCE_PRINCIPLES.DELETE_FINANCE_PRINCIPLE).params({id}).execute()
        refreshPrinciples()
    } catch(err) {
        toast.add({
            title: "Error delete income source",
            description:`Error while delete income source`, 
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
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Principes Financiers</h2>
                    <p class="text-sm text-gray-500">Règles et contraintes budgétaires automatiques</p>
                </div>
                <UButton 
                    label="Nouveau principe" 
                    icon="i-lucide-plus" 
                    size="md"
                    color="primary"
                    @click="openModalFinancePrinciple()"
                />
            </div>

            <div class="max-w-md">
                <UInput 
                    icon="i-lucide-search" 
                    placeholder="Rechercher un principe..." 
                    size="md"
                />
            </div>

            <!-- Table view for principles -->
            <div class="overflow-hidden border border-gray-200 rounded-xl">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Nom
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Description
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Type
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Rigueur
                            </th>
                            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Actions
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <tr v-for="principle of data?.financePrinciples" 
                            :key="principle.id"
                            class="hover:bg-gray-50 transition-colors">
                            <td class="px-6 py-4 whitespace-nowrap">
                                <div class="flex items-center gap-3">
                                    <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-indigo-50">
                                        <UIcon 
                                            name="i-lucide-shield-check" 
                                            class="w-4 h-4 text-indigo-600"
                                        />
                                    </div>
                                    <span class="text-sm font-medium text-gray-900">
                                        {{ principle.name }}
                                    </span>
                                </div>
                            </td>

                            <td class="px-6 py-4">
                                <span class="text-sm text-gray-600 line-clamp-2">
                                    {{ principle.description || 'Aucune description' }}
                                </span>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                    {{ data?.principleTypes?.find(t => t.id === principle.targetType)?.value || principle.targetType }}
                                </span>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <div class="flex items-center gap-2">
                                    <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden max-w-[80px]">
                                        <div 
                                            class="h-full rounded-full transition-all"
                                            :class="{
                                                'bg-green-500': principle.strictness <= 3,
                                                'bg-yellow-500': principle.strictness > 3 && principle.strictness <= 7,
                                                'bg-red-500': principle.strictness > 7
                                            }"
                                            :style="{ width: (principle.strictness * 10) + '%' }">
                                        </div>
                                    </div>
                                    <span class="text-xs font-medium text-gray-600">
                                        {{ principle.strictness }}/10
                                    </span>
                                </div>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap text-right">
                                <div class="flex items-center justify-end gap-2">
                                    <UButton 
                                        variant="ghost" 
                                        color="neutral" 
                                        icon="i-lucide-pencil" 
                                        size="xs"
                                        @click="openModalFinancePrinciple(principle.id)" 
                                    />
                                    <UButton 
                                        variant="ghost" 
                                        color="error" 
                                        icon="i-lucide-trash-2" 
                                        size="xs"
                                        @click="onDeletePrinciple(principle.id)"
                                    />
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Empty state -->
            <div v-if="!data?.financePrinciples || data.financePrinciples.length === 0" 
                class="text-center py-12 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
                <UIcon name="i-lucide-shield-check" class="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p class="text-gray-500 text-sm">Aucun principe financier configuré</p>
                <UButton 
                    label="Créer le premier principe" 
                    size="sm" 
                    class="mt-3"
                    @click="openModalFinancePrinciple()"
                />
            </div>
        </div>
    </UCard>
</template>