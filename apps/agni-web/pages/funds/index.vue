<script setup lang="ts">
import { 
    ModalFund,
    ModalFundAmount,
} from "#components"
import { ref } from "vue"
import { fetchAccounts } from "~/composables/api/accounts"
import { fetchFundSummary } from "~/composables/api/analytics"
import { fetchFunds, fetchFund, useDeleteFund } from "~/composables/api/funds"
import { fetchAllGoal } from "~/composables/api/goals"
import { fundToFundCard, fundToFundForm } from "~/mappers/fund"
import { goalToFundGoalCards } from "~/mappers/goal"
import type { QueryFilterFundRequest } from "~/types/api/fund"
import type { GoalQueryFilterRequest } from "~/types/api/goal"
import type { Fund } from "~/types/ui/fund"
import type { Goal } from "~/types/ui/goal"

const loadingSummary = ref(false)
const isLoading = ref(false)
const isLoadingGoal = ref(false)
const filter = reactive<QueryFilterFundRequest>({
    offset: 0,
    limit: 5,
    queryAll: false
})

const filterGoal = reactive<GoalQueryFilterRequest>({
    offset: 0,
    limit: 3,
    queryAll: false,
    type: 'Fund'
})
const funds = ref<Fund[]>([])
const totalFund = ref(0)

const goalSummaries = ref<Goal[]>([])
const totalGoalSummary = ref(0)

const { data: accounts } = useAsyncData('funds+accounts', async () => {
    const res = await fetchAccounts({ limit: 10, offset: 0, queryAll: true})

    return res.items
})

const { data: summary } = useAsyncData('page-fund-summary', async () => {
    loadingSummary.value = true
    const res = await fetchFundSummary()
    loadingSummary.value = false

    return {
        totalTarget: res.totalTarget,
        totalBalance: res.totalBalance,
        remain: res.totalTarget - res.totalBalance
    }
}, {
    watch: [funds]
})


const toast = useToast()
const overlay = useOverlay()
const modalFund = overlay.create(ModalFund)
const modalAmountFund = overlay.create(ModalFundAmount)

async function updateFundList(id: string) {
    const fund = await fetchFund(id)
    const index = funds.value.findIndex(i => i.id == id)
    if (index >= 0 ) {
        funds.value[index] = fund
    } else {
        funds.value.unshift(fund)
    }
}

async function updateGoalList() {
    goalSummaries.value = []
    await getAllGoals()
}

async function openModalFund(goalId?: string) {
    let fund: Fund | undefined = undefined
    if (goalId) {
        fund = await fetchFund(goalId) 
    }

    modalFund.open({
        fundId: fund?.id,
        initData: fund ? fundToFundForm(fund) : undefined,
        onClose: async (returnId) => {
            if (returnId) {
                updateGoalList()
                await updateFundList(returnId)
            }
        }
    }).result 
}

async function openModalFundAmount (isIncrease: boolean, fundId: string) {
    try {
        let fund = await fetchFund(fundId) 
        
        modalAmountFund.open({
            isIncrease: isIncrease,
            fund: fund,
            fundAccountId: fund.accountId,
            onClose: (close) => {
                if (close) {
                    updateGoalList()
                    updateFundList(fundId)
                }
            }
        })
    } catch(err: any) {
        toast.add({
            color: err,
            title: 'Erreur',
            description: err.message
        })
    }
}

const deleteAccountDepositId = ref('')
const deletePopOverOpen = ref(false)
const deletePopOverGoalId = ref<string>()

function openModalDeleteFund(fundId: string) {
    deletePopOverGoalId.value = fundId
    deletePopOverOpen.value = true
}

const onDeleteFund = async (fundId: string) => {
    try {
        await useDeleteFund(fundId, { accountId: deleteAccountDepositId.value })
        const indexToRemove = funds.value.findIndex(i => i.id === fundId)
        if (indexToRemove >= 0)  {
            funds.value = funds.value.filter(i => i.id !== fundId)
            totalFund.value -= 1
            if (filter.offset === funds.value.length)
                await getAllFunds()
        }

        deletePopOverOpen.value = false
        deletePopOverGoalId.value = undefined
        toast.add({
            title: 'Succès',
            description: 'Fund supprimé',
            color: 'success'
        })
    } catch(err: any) {
        toast.add({
            title: 'Erreur',
            description: 'Impossible de supprimer l\'objectif ' + err.message,
            color: 'error'
        })
    }
} 

async function showMoreFund() {
    if (funds.value.length === totalFund.value)
        return 

    filter.offset = funds.value.length
}

async function showMoreGoalSummary() {
    if (goalSummaries.value.length === totalGoalSummary.value)
        return 

    filterGoal.offset = goalSummaries.value.length
}

async function getAllFunds() {
    isLoading.value = true
    try {
        var res = await fetchFunds(filter)
        funds.value.push(...res.items) 
        totalFund.value = res.total
    } catch(err: any) {
        toast.add({
            title: 'Erreur Funds',
            description: err.message,
            color: 'error'
        })
    } finally {
        isLoading.value = false
    }
}

async function getAllGoals() {
    isLoadingGoal.value = true
    try {
        var res = await fetchAllGoal(filterGoal)
        goalSummaries.value.push(...res.items) 
        totalGoalSummary.value = res.total
    } catch(err: any) {
        toast.add({
            title: 'Erreur Goals',
            description: err.message,
            color: 'error'
        })
    } finally {
        isLoading.value = false
    }
}

watch(filter, () => {
    getAllFunds()
}, { immediate: true })

watch(filterGoal, () => {
    getAllGoals()
}, { immediate: true })

</script>

<template>
    <div class="goals-page p-5">
        <div class="grid md:grid-cols-3 gap-5 mb-10 grid-cols-1">
            <UiBannerAccountant 
                title="Objectif Total"
                :icon="{ name: 'i-lucide-target', backgroundColor: 'rgba(168, 85, 247, 0.1)', fontColor: '#a855f7'}"
                :amount="summary?.totalTarget ?? 0"
            />

            <UiBannerAccountant 
                title="Économisé"
                :icon="{ name: 'i-lucide-piggy-bank', backgroundColor: 'rgba(34, 197, 94, 0.1)', fontColor: '#22c55e'}"
                :amount="summary?.totalBalance ?? 0"
            />

            <UiBannerAccountant 
                title="Restant"
                :icon="{ name: 'i-lucide-trending-up', backgroundColor: 'rgba(59, 130, 246, 0.1)', fontColor: '#3b82f6'}"
                :amount="summary?.remain ?? 0"
            />
        </div>

        <div class="flex justify-end items-center mb-8 ">
            <UButton 
                icon="i-lucide-plus" 
                label="Nouvel Objectif" 
                size="xl"
                class="add-button"
                @click="openModalFund()"
            />
        </div>

        <div class="w-full max-w-full flex gap-4 overflow-x-auto overflow-y-hidden pb-4 mb-8 flex-nowrap shrink-0">
            <UiFundCardSummaryGoal 
                v-for="goal in  goalSummaries.map(i => goalToFundGoalCards(i))" 
                :key="goal.id"
                class="w-72 shrink-0" 
                :goal="goal"
            />
            <!--  -->
            <div 
                v-if="goalSummaries.length < totalGoalSummary"
                class="min-w-48 p-5 rounded-xl bg-gray-50 border border-dashed border-gray-300 h-full flex justify-center cursor-pointer hover:shadow-xs"
                @click="showMoreGoalSummary()" >
                <div class="flex items-center my-8">
                    <span>Afficher plus</span> 
                    <UIcon name="i-lucide-arrow-right" />
                </div>                
            </div>
        </div>

        <!-- Cards View -->
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-[repeat(auto-fill,minmax(280px,1fr))] md:grid-cols-[repeat(auto-fill,minmax(340px,1fr))] md:gap-6">
            <TransitionGroup name="goal-list">
                <UiFundCard 
                    v-for="fund in funds" 
                    :key="fund.id"
                    :fund="fundToFundCard(fund)"
                    @add-goal=""
                    @refresh="refreshed =>  {
                        if (refreshed) {
                            updateGoalList()
                            updateFundList(fund.id)
                        }
                    }"
                    @update="openModalFund(fund.id)"
                    @delete="() => openModalDeleteFund(fund.id)"
                    @increase-amount="openModalFundAmount(true, fund.id)"
                    @decrease-amount="openModalFundAmount(false, fund.id)"
                />
            </TransitionGroup>

            <div 
                v-if="funds.length < totalFund"
                class="p-5 rounded-xl bg-gray-50 border border-dashed border-gray-300 h-full flex justify-center cursor-pointer hover:shadow-xs"
                @click="showMoreFund()" >
                <div class="flex items-center my-8">
                    <span>Afficher plus</span> 
                    <UIcon name="i-lucide-arrow-right" />
                </div>                
            </div>
            <UiEmptyState 
                v-if="funds.length === 0 && totalFund == 0"
                icon="i-lucide-target"
                title="Aucun fond"
                description="Commencez par créer votre premier fond"
                @new="openModalFund()"
            />
        </div>

        <!-- Delete Modal -->
        <UModal v-model:open="deletePopOverOpen" class="delete-modal">
            <template #content>
                <div class="modal-content">
                    <div class="modal-header">
                        <UIcon name="i-lucide-trash-2" class="modal-icon" />
                        <h3 class="modal-title">Supprimer l'objectif</h3>
                    </div>
                    <p class="modal-description">
                        Sélectionnez le compte où déposer les fonds de cet objectif
                    </p>
                    <USelect 
                        v-model="deleteAccountDepositId" 
                        value-key="value" 
                        label-key="label" 
                        placeholder="Sélectionner un compte"
                        :items="accounts?.map(i => ({ label: i.title, value: i.id }))" 
                        size="xl"
                    />
                    <div class="modal-actions">
                        <UButton 
                            label="Annuler" 
                            variant="outline"
                            color="neutral"
                            block
                            @click="() => { deletePopOverOpen = false }"
                        />
                        <UButton 
                            label="Supprimer" 
                            color="error"
                            block
                            @click="() => {
                                deletePopOverGoalId ? onDeleteFund(deletePopOverGoalId) : null
                            }"
                        />
                    </div>
                </div>
            </template>
        </UModal>
    </div>
</template>