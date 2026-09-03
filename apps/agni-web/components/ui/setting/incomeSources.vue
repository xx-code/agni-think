<script setup lang="ts">
import type { NuxtError } from '#app'
import { ModalEditIncomeSource } from '#components'
import { getLocalTimeZone } from '@internationalized/date'
import { incomeSourceResponseToIncomeSource, listIncomeSourcesResponseToListIncomeSources } from '~/mappers/incomeSource'
import { API_ROUTES } from '~/shared/routes'
import type { ListResponse, CreatedRequest } from '~/types/api'
import type { GetIncomeSourceResponse } from '~/types/api/incomeSource'
import type { GetInternalTypeResponse } from '~/types/api/internal'
import type { EditIncomeSourceType, IncomeSourceType } from '~/types/ui/incomeSource'

const overlay = useOverlay()
const toast = useToast()
const modalEditIncomeSource = overlay.create(ModalEditIncomeSource) 

const { data, error: errorIncomeSources, refresh: refreshIncomeSources } = useAsyncData('income-sources+all+setting', async () => {
    const [ incomeSourceFrequencyTypes, incomeSourceTypes,  incomeSources] = await Promise.all([
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.INCOME_SOURCE_FREQUENCY_TYPE).execute(),
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.INCOME_SOURCE_TYPE).execute(),
        ApiLinkBuilder.route<ListResponse<GetIncomeSourceResponse>>(API_ROUTES.INCOME_SOURCES.GET_INCOME_SOURCES).query({ queryAll: true, limit: 0, offset: 0}).mapper(listIncomeSourcesResponseToListIncomeSources).execute()
    ])

    return {
        incomeSourceFrequencyTypes,
        incomeSourceTypes,
        incomeSources: incomeSources.items
    }
})

const openModalIncomeSource = async (id?: string) => {  
    let source:IncomeSourceType|undefined=undefined;
    if (id) {
        source = await ApiLinkBuilder.route<GetIncomeSourceResponse>(API_ROUTES.INCOME_SOURCES.GET_INCOME_SOURCE).params({id}).mapper(incomeSourceResponseToIncomeSource).execute(); 
    }

    modalEditIncomeSource.open({
        incomeSource: source, 
        onSubmit: onSubmitIncomeSource
    });
}



async function onSubmitIncomeSource(value: EditIncomeSourceType, oldValue?: IncomeSourceType) {
    try {
        if (oldValue) {
            await ApiLinkBuilder.route(API_ROUTES.INCOME_SOURCES.UPDATE_INCOME_SOURCE).params({id: oldValue.id}).body({
                title: value.title,
                annualGrossAmount: value.annualGrossAmount,
                endDate: value.endDate?.toDate(getLocalTimeZone()).toISOString(),
                linkedAccountId: value.linkedAccountId,
                otherRate: value.otherRate,
                payFrequencyType: value.payFrequencyType,
                reliabilityLevel: value.reliabilityLevel,
                startDate: value.startDate.toDate(getLocalTimeZone()).toISOString(),
                taxRate: value.taxRate,
                type: value.type
            }).execute()
        } else {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.INCOME_SOURCES.CREATE_INCOME_SOURCE).body({
                title: value.title,
                annualGrossAmount: value.annualGrossAmount,
                endDate: value.endDate?.toDate(getLocalTimeZone()).toISOString(),
                linkedAccountId: value.linkedAccountId,
                otherRate: value.otherRate,
                payFrequencyType: value.payFrequencyType,
                reliabilityLevel: value.reliabilityLevel,
                startDate: value.startDate.toDate(getLocalTimeZone()).toISOString(),
                taxRate: value.taxRate,
                type: value.type
            }).execute()
        }
        await refreshIncomeSources()
    } catch(err) {
        const nuxtError = err as NuxtError
        toast.add({
            title: "Error income source",
            // description: nuxtError.data, 
            color: 'error'
        });
    }
}

const onDeleteIncomeSource = async (id: string) => {
    try {
        await ApiLinkBuilder.route(API_ROUTES.INCOME_SOURCES.DELETE_INCOME_SOURCE).params({id}).execute()
        refreshIncomeSources()
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
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Sources de Revenus</h2>
                    <p class="text-sm text-gray-500">Gestion des revenus et salaires</p>
                </div>
                <UButton 
                    label="Nouvelle source" 
                    icon="i-lucide-plus" 
                    size="md"
                    color="primary"
                    @click="openModalIncomeSource()"
                />
            </div>

            <div class="max-w-md">
                <UInput 
                    icon="i-lucide-search" 
                    placeholder="Rechercher une source..." 
                    size="md"
                />
            </div>

            <!-- Table view for income sources -->
            <div class="overflow-hidden border border-gray-200 rounded-xl">
                <table class="min-w-full divide-y divide-gray-200">
                    <thead class="bg-gray-50">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Titre
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Type
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Fréquence
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Fiabilité
                            </th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Montant Net Estimé
                            </th>
                            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Actions
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-200">
                        <tr v-for="source of data?.incomeSources" 
                            :key="source.id"
                            class="hover:bg-gray-50 transition-colors">
                            <td class="px-6 py-4 whitespace-nowrap">
                                <div class="flex items-center gap-3">
                                    <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-emerald-50">
                                        <UIcon 
                                            name="i-lucide-trending-up" 
                                            class="w-4 h-4 text-emerald-600"
                                        />
                                    </div>
                                    <div>
                                        <span class="text-sm font-medium text-gray-900 block">
                                            {{ source.title }}
                                        </span>
                                        <span class="text-xs text-gray-500">
                                            Depuis {{ new Date(source.startDate).toLocaleDateString('fr-CA') }}
                                        </span>
                                    </div>
                                </div>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                    {{ data?.incomeSourceTypes?.find(t => t.id === source.type)?.value || source.type }}
                                </span>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                                    {{ data?.incomeSourceFrequencyTypes?.find(t => t.id === source.payFrequencyType)?.value || source.payFrequencyType }}
                                </span>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <div class="flex items-center gap-2">
                                    <div class="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden max-w-[80px]">
                                        <div 
                                            class="h-full rounded-full transition-all"
                                            :class="{
                                                'bg-red-500': source.reliabilityLevel <= 3,
                                                'bg-yellow-500': source.reliabilityLevel > 3 && source.reliabilityLevel <= 7,
                                                'bg-green-500': source.reliabilityLevel > 7
                                            }"
                                            :style="{ width: (source.reliabilityLevel * 10) + '%' }">
                                        </div>
                                    </div>
                                    <span class="text-xs font-medium text-gray-600">
                                        {{ source.reliabilityLevel }}/10
                                    </span>
                                </div>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="text-sm font-semibold text-emerald-600">
                                    {{ source.estimateNextNetAmount ? source.estimateNextNetAmount.toFixed(2) + ' $' : 'N/A' }}
                                </span>
                            </td>

                            <td class="px-6 py-4 whitespace-nowrap text-right">
                                <div class="flex items-center justify-end gap-2">
                                    <UButton 
                                        variant="ghost" 
                                        color="neutral" 
                                        icon="i-lucide-pencil" 
                                        size="xs"
                                        @click="openModalIncomeSource(source.id)" 
                                    />
                                    <UButton 
                                        variant="ghost" 
                                        color="error" 
                                        icon="i-lucide-trash-2" 
                                        size="xs"
                                        @click="onDeleteIncomeSource(source.id)"
                                    />
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Empty state -->
            <div v-if="!data?.incomeSources || data?.incomeSources.length === 0" 
                class="text-center py-12 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
                <UIcon name="i-lucide-trending-up" class="w-12 h-12 text-gray-300 mx-auto mb-3" />
                <p class="text-gray-500 text-sm">Aucune source de revenus configurée</p>
                <UButton 
                    label="Créer la première source" 
                    size="sm" 
                    class="mt-3"
                    @click="openModalIncomeSource()"
                />
            </div>
        </div>
    </UCard>
</template>