<script setup lang="ts">
import { ModalEditBudget, SlideOverQuickInvoicesView } from "#components"
import { getLocalTimeZone } from "@internationalized/date"
import { computed, ref } from "vue"
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder'
import { API_ROUTES } from '~/shared/routes'
import { budgetFilterToBudgetQueryRequest, budgetResponseToBudget, listBudgetsResponseToListBudgets, budgetToBudgetCard } from '~/mappers/budget'
import type { CreatedRequest, ListResponse } from '~/types/api'
import type { GetBudgetResponse } from '~/types/api/budget'
import type { BudgetTotalSummaryResponse } from '~/types/api/analytics'
import type { BudgetFilter, BudgetType, EditBudgetType } from "~/types/ui/budget"

const isLoadingSummary = ref(false)

const filter = reactive<BudgetFilter>({
    offset: 0,
    limit: 5,
    queryAll: false,
    periodTypes: []
})

const budgets = ref<BudgetType[]>([])
const totalBudget = ref(0)
const isLoading = ref(false)

const { data: summary } = useAsyncData('page-budget-summary', async () => {
    isLoadingSummary.value = true
    const res = await ApiLinkBuilder.route<BudgetTotalSummaryResponse>(API_ROUTES.ANALYTICS.BUDGET_TOTAL_SUMMARY).execute()
    isLoadingSummary.value = false

    return {
        total: res.totalBudget,
        spend: res.totalSpend,
        remain: res.totalBudget - res.totalSpend
    }
}, {
    watch: [budgets]
})

const overlay = useOverlay()
const modalEditBudget = overlay.create(ModalEditBudget)
const slideOverQuickInvoices = overlay.create(SlideOverQuickInvoicesView)
const toast = useToast()

const listTypeDateDisplay = computed(() => ([
    {
        label: 'Jours' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            onAddPeriodTag('Day', 'Jours')
        } 
    },
    {
        label: 'Semaine' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            onAddPeriodTag('Week', 'Semaines')
        } 
    },
    {
        label: 'Mois' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            onAddPeriodTag('Month', 'Mois')
        } 
    },
    {
        label: 'Année' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            onAddPeriodTag( 'Year',  'Années')
        } 
    }
]))

function onAddPeriodTag(id: string, label: string) {
    if (!filter.periodTypes?.find(i => i.id === id)) {
        budgets.value = []
        totalBudget.value = 0
        filter.periodTypes?.push({id: id, label: label}) 
    }
}

function onRemovePeriodTag(id: string) {
    budgets.value = []
    totalBudget.value = 0
    filter.periodTypes = filter.periodTypes?.filter(i => i.id !== id)
}

async function onSubmitBudget(value: EditBudgetType, oldValue?: BudgetType) {
    try {
        let id;
        if (oldValue) {
            await ApiLinkBuilder.route(API_ROUTES.BUDGETS.UPDATE_BUDGET).params({id: oldValue.id}).body({
                title: value.title,
                target: value.target,
                schedule: {
                    repeater: value.repeater,
                    dueDate: value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                }
            }).execute()
            id = oldValue.id
        } else {
            const res = await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.BUDGETS.CREATE_BUDGET).body({
                title: value.title,
                target: value.target,
                schedule: {
                    repeater: value.repeater,
                    dueDate: value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                }
            }).execute()
            id = res.newId 
        } 
            
        updateBudgetList(id)

        toast.add({
            title: 'Succès',
            description: oldValue ? 'Budget mis à jour' : 'Budget créé',
            color: 'success'
        })
    } catch(err) {
        toast.add({
            title: 'Erreur budget',
            description: 'Erreur lors de la soumission: ' + err,
            color: 'error'
        })
    }
}

async function openModalBudget(budgetId?: string) { 
    let budget: BudgetType | undefined
    if (budgetId) {
        budget = await ApiLinkBuilder.route<GetBudgetResponse>(API_ROUTES.BUDGETS.GET_BUDGET).params({id: budgetId}).mapper(budgetResponseToBudget).execute()
    }

    modalEditBudget.open({
        budget: budget,
        onSubmit: onSubmitBudget
    }) 
}

const onDeleteBudget = async (budgetId: string) => {
    try {
        await ApiLinkBuilder.route(API_ROUTES.BUDGETS.DELETE_BUDGET).params({id: budgetId}).execute()
        const indexToRemove = budgets.value.findIndex(i => i.id === budgetId)
        if (indexToRemove >= 0)  {
            budgets.value = budgets.value.filter(i => i.id !== budgetId)
            totalBudget.value -= 1
            if (filter.offset === budgets.value.length)
                await getAllBudgets()
        }
        toast.add({
            title: 'Succès',
            description: 'Budget supprimé',
            color: 'success'
        })
    } catch (err) {
        toast.add({
            title: 'Erreur',
            description: 'Impossible de supprimer le budget',
            color: 'error'
        })
    }
}

const openInvoiceView = async (budgetId: string) => {
    try {
        slideOverQuickInvoices.open({
            budgetIds: [budgetId]
        })
    } catch (err) {
        console.log(err)
    } 
}

async function updateBudgetList(id: string) {
    const budget = await ApiLinkBuilder.route<GetBudgetResponse>(API_ROUTES.BUDGETS.GET_BUDGET).params({id}).mapper(budgetResponseToBudget).execute()
    const index = budgets.value.findIndex(i => i.id == id)
    if (index >= 0 ) {
        budgets.value[index] = budget
    } else {
        budgets.value.unshift(budget)
    }
}

async function showMoreBudget() {
    if (budgets.value.length === totalBudget.value)
        return 

    filter.offset = budgets.value.length
}

async function getAllBudgets() {
    isLoading.value = true
    try {
        var res = await ApiLinkBuilder.route(API_ROUTES.BUDGETS.GET_BUDGETS).query(budgetFilterToBudgetQueryRequest(filter)).mapper(listBudgetsResponseToListBudgets).execute()
        budgets.value.push(...res.items) 
        totalBudget.value = res.total
    } catch(err: any) {
        toast.add({
            title: 'Erreur Budgets',
            description: err.message,
            color: 'error'
        })
    } finally {
        isLoading.value = false
    }
}


watch(filter, () => {
    getAllBudgets()
}, { immediate: true })

</script>

<template>
    <UiPage>
        <UiPageHeader 
            title="Mes Budgets"
            :button="
                {
                    icon: 'i-lucide-plus',
                    label: 'Ajouter un budget'
                }
            "
            subtitle="Gerer tous les budgets"
            @click-button="openModalBudget()"
        />

        <div class="grid md:grid-cols-3 gap-5 mb-10 grid-cols-1">
            <UiBannerAccountant 
                title="Budget Total"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(168, 85, 247, 0.1)', fontColor: '#a855f7'}"
                :amount="summary?.total ?? 0"
            />

            <UiBannerAccountant 
                title="Dépensé"
                :icon="{ name: 'i-lucide-piggy-bank', backgroundColor: 'rgba(242, 65, 71, 0.1)', fontColor: '#fb2c36'}"
                :amount="summary?.spend ?? 0"
            />

            <UiBannerAccountant 
                title="Restant"
                :icon="{ name: 'i-lucide-trending-up', backgroundColor: 'rgba(34, 197, 94, 0.1)', fontColor: '#22c55e'}"
                :amount="summary?.remain ?? 0"
            />
        </div>

        <!-- Action Bar -->
        <div class="flex justify-between items-center flex-wrap">
            <div class="flex">
                <div class="flex items-center gap-1">
                    <div class="">
                        <UButton 
                            icon="i-lucide-calendar-1" 
                            size="xl" 
                            class="p-3"
                            variant="outline" 
                            color="neutral"
                        />
                    </div>

                    <UDropdownMenu :items="listTypeDateDisplay" @click.stop>
                        <UButton 
                            icon="i-lucide-calendar-1" 
                            trailing-icon="i-lucide-chevron-down"
                            size="xl" 
                            variant="outline" 
                            label="Periode"
                            color="neutral"
                        />
                    </UDropdownMenu>
                    
                    <USeparator class="mx-2" orientation="vertical" />
                </div> 

                <div class="flex items-center flex-wrap gap-2">
                    <UiTagButton 
                        v-for="period in filter.periodTypes"
                        :key="period.id"
                        removable
                        :label="period.label"
                        @remove="onRemovePeriodTag(period.id)"
                    />
                </div>
            </div>

            <!-- <div>
                <UButton 
                    icon="i-lucide-plus" 
                    label="Nouveau Budget" 
                    size="xl" 
                    class="add-button"
                    @click="openModalBudget()"
                />
            </div>  -->
        </div>

        <!-- Budget Grid -->
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-[repeat(auto-fill,minmax(280px,1fr))] md:grid-cols-[repeat(auto-fill,minmax(340px,1fr))] md:gap-6">
            <TransitionGroup name="budget-list">
                <UiBudgetCard 
                    v-for="budget in budgets"
                    :key="budget.id"
                    :budget="budgetToBudgetCard(budget)"
                    @click="() => openInvoiceView(budget.id)"
                    @update="(id) => openModalBudget(id)"
                    @delete="(id) => onDeleteBudget(id)"
                />
            </TransitionGroup>

            <div 
                v-if="budgets.length < totalBudget"
                class="p-5 rounded-xl bg-gray-50 border border-dashed border-gray-300 h-full flex justify-center cursor-pointer hover:shadow-xs"
                @click="showMoreBudget()" >
                <div class="flex items-center my-8">
                    <span>Afficher plus</span> 
                    <UIcon name="i-lucide-arrow-right" />
                </div>                
            </div>

            <UiEmptyState 
                v-if="budgets?.length === 0 && totalBudget == 0"
                icon="i-lucide-target"
                title="Aucun fond"
                description="Commencez par créer votre premier fond"
                @new="openModalBudget()"
            />
        </div>
    </UiPage>
</template>
