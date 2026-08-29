<script setup lang="ts">
import type { ListResponse } from '~/types/api';
import type { GetTagResponse } from '~/types/api/tag';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import { listTagsResponseToListTags } from '~/mappers/tag';
import { budgetFilterToBudgetQueryRequest, listBudgetsResponseToListBudgets } from '~/mappers/budget';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';

import type { FormFilterTransaction } from '~/types/ui/component';

const emit = defineEmits<{
    (e: 'submit', value: FormFilterTransaction): void
}>();

const open = ref(false);

const { data: utils, status } = useAsyncData('utils+edit-invoices', async () => {
    const query = { offset: 0, limit: 0, queryAll: true, isSystem: false }
    const [tags, budgets, transactionTypes] = await Promise.all([
        ApiLinkBuilder
            .route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS)
            .query(query)
            .mapper(listTagsResponseToListTags)
            .execute(),
        ApiLinkBuilder
            .route(API_ROUTES.BUDGETS.GET_BUDGETS)
            .query(budgetFilterToBudgetQueryRequest(query))
            .mapper(listBudgetsResponseToListBudgets)
            .execute(),
        ApiLinkBuilder
            .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.TRANSACTION_TYPE)
            .execute()
    ])

    return { tags, budgets, transactionTypes }
})

// TODO: Refactor
const selectedBudgetIds = ref<string[]>([]);
const selectedTagIds = ref<string[]>([]);
const selectedStatus = ref<string>('Complete')
const selectedTypeTransaction = ref<string[]>([])

const filters = ref<{
    filterPrice: boolean,
    filterStatus: boolean
}>({
    filterPrice: false,
    filterStatus: false,
})

const minAmount = ref<number | undefined>()
const maxAmount = ref<number | undefined>()

// Chaque filtre actif compte pour 1, quel que soit le nombre de valeurs sélectionnées à l'intérieur.
// C'est ce nombre qui alimente le badge sur le bouton déclencheur ("Plus de filtres  3").
const numberFilter = computed(() => {
    let count = 0
    if (selectedBudgetIds.value.length > 0) count += 1
    if (selectedTagIds.value.length > 0) count += 1
    if (selectedTypeTransaction.value.length > 0) count += 1
    if (filters.value.filterStatus) count += 1
    if (filters.value.filterPrice) count += 1
    return count
})

const hasAnyFilter = computed(() => numberFilter.value > 0)

function buildPayload(): FormFilterTransaction {
    return {
        budgetIds: selectedBudgetIds.value,
        types: selectedTypeTransaction.value,
        status: filters.value.filterStatus ? selectedStatus.value : undefined,
        tagIds: selectedTagIds.value,
        minPrice: filters.value.filterPrice ? minAmount.value : undefined,
        maxPrice: filters.value.filterPrice ? maxAmount.value : undefined
    }
}


function apply() {
    emit('submit', buildPayload());
    open.value = false;
}

function clean() {
    filters.value.filterPrice = false;
    filters.value.filterStatus = false;
    selectedStatus.value = 'Complete';
    selectedTypeTransaction.value = [];
    selectedBudgetIds.value = [];
    selectedTagIds.value = [];
    minAmount.value = undefined;
    maxAmount.value = undefined;
    emit('submit', buildPayload());
}
</script>

<template>
    <UDrawer v-model:open="open" direction="right" inset>
        <UButton
            color="neutral"
            variant="outline"
            icon="i-lucide-sliders-horizontal"
            size="lg"
            label="Plus de filtres"
        >
            <template #trailing>
                <UBadge v-if="hasAnyFilter" size="sm" color="primary" variant="solid">
                    {{ numberFilter }}
                </UBadge>
            </template>
        </UButton>

        <template #content>
            <div class="flex flex-col h-full min-w-96">
                <!-- Header -->
                <div class="flex items-start justify-between px-5 pt-5 pb-4 border-b border-default">
                    <div>
                        <h2 class="text-base font-medium text-highlighted">Filtres avancés</h2>
                        <p class="text-sm text-muted mt-0.5">Affinez la liste des transactions affichées</p>
                    </div>
                    <UButton
                        color="neutral"
                        variant="ghost"
                        icon="i-lucide-x"
                        size="sm"
                        aria-label="Fermer le panneau de filtres"
                        @click="() => {open = false}"
                    />
                </div>

                <!-- Body -->
                <div class="flex-1 overflow-y-auto px-5 py-5 space-y-6">

                    <!-- Section: filtres de sélection -->
                    <div class="space-y-4">
                        <div>
                            <USkeleton v-if="status === 'pending'" class="h-9 w-full" />
                            <UiInvoiceSelectMenu
                                v-else
                                icon="i-lucide-tag"
                                title="Tous les tags"
                                :items="utils?.tags.items.map(i => ({ label: i.value, value: i.id })) ?? []"
                                v-model="selectedTagIds"
                                placeholder="Tous les tags"
                            />
                        </div> 

                        <div>
                            <USkeleton v-if="status === 'pending'" class="h-9 w-full" />
                            <UiInvoiceSelectMenu
                                v-else
                                icon="i-lucide-wallet"
                                :items="utils?.budgets.items.map(i => ({ label: i.title, value: i.id })) ?? []"
                                v-model="selectedBudgetIds"
                                title="Tous les budgets"
                            />
                        </div>
                        
                        <div>
                            <USkeleton v-if="status === 'pending'" class="h-9 w-full" />
                            <UiInvoiceSelectMenu
                                v-else
                                icon="i-lucide-arrow-left-right"
                                :items="utils?.transactionTypes.map(i => ({ label: i.value, value: i.id })) ?? []"
                                v-model="selectedTypeTransaction"
                                title="Tous les types"
                            />
                        </div>
                    </div>

                    <USeparator />

                    <!-- Section: statut -->
                    <div class="space-y-3">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-2">
                                <UIcon name="i-lucide-check-circle-2" class="size-4 text-muted" />
                                <span class="text-sm font-medium text-highlighted">Filtrer par statut</span>
                            </div>
                            <USwitch v-model="filters.filterStatus" />
                        </div>
                        <UTabs
                            v-if="filters.filterStatus"
                            v-model="selectedStatus"
                            value-key="value"
                            :items="[{ label: 'Complété', value: 'Complete' }, { label: 'En attente', value: 'Pending' }]"
                        />
                    </div>

                    <USeparator />

                    <!-- Section: montant -->
                    <div class="space-y-3">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center gap-2">
                                <UIcon name="i-lucide-dollar-sign" class="size-4 text-muted" />
                                <span class="text-sm font-medium text-highlighted">Filtrer par montant</span>
                            </div>
                            <USwitch v-model="filters.filterPrice" />
                        </div>
                        <div v-if="filters.filterPrice" class="flex items-center gap-2">
                            <UInput
                                v-model="minAmount"
                                type="number"
                                :min="0"
                                placeholder="Min"
                                leading-icon="i-lucide-dollar-sign"
                                class="flex-1"
                            />
                            <span class="text-muted text-sm">à</span>
                            <UInput
                                v-model="maxAmount"
                                type="number"
                                :min="minAmount"
                                placeholder="Max"
                                leading-icon="i-lucide-dollar-sign"
                                class="flex-1"
                            />
                        </div>
                    </div>
                </div>

                <!-- Footer sticky -->
                <div class="flex items-center gap-3 px-5 py-4 border-t border-default">
                    <UButton
                        label="Nettoyer"
                        icon="i-lucide-rotate-ccw"
                        variant="outline"
                        color="neutral"
                        :disabled="!hasAnyFilter"
                        class="flex-1 justify-center"
                        @click="clean"
                    />
                    <UButton
                        label="Filtrer"
                        color="primary"
                        class="flex-1 justify-center"
                        @click="apply"
                    />
                </div>
            </div>
        </template>
    </UDrawer>
</template>