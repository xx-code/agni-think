<script setup lang="ts">
import { 
    ModalFund,
    ModalFundAmount,
} from "#components"
import { ref } from "vue"
import { fetchAccounts } from "~/composables/api/accounts"
import { fetchFundSummary } from "~/composables/api/analytics"
import { fetchFunds, fetchFund, useDeleteFund } from "~/composables/api/funds"
import { fundToFundForm } from "~/mappers/fund"
import type { QueryFilterFundRequest } from "~/types/api/fund"
import type { FundType } from "~/types/ui/fund"

const loadingSummary = ref(false)
const isLoading = ref(false)
const filter = reactive<QueryFilterFundRequest>({
    offset: 0,
    limit: 1,
    queryAll: false
})
const funds = ref<FundType[]>([])
const totalFund = ref(0)

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

async function openModalFund(goalId?: string) {
    let fund: FundType | undefined = undefined
    if (goalId) {
        fund = await fetchFund(goalId) 
    }

    modalFund.open({
        fundId: fund?.id,
        initData: fund ? fundToFundForm(fund) : undefined,
        onClose: async (returnId) => {
            if (returnId) {
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

async function getAllFunds() {
    isLoading.value = true
    try {
        var res = await fetchFunds(filter)
        console.log(res.items)
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

watch(filter, () => {
    getAllFunds()
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

        <!-- Cards View -->
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-[repeat(auto-fill,minmax(280px,1fr))] md:grid-cols-[repeat(auto-fill,minmax(340px,1fr))] md:gap-6">
            <TransitionGroup name="goal-list">
                <UiFundCard 
                    v-for="fund in funds" 
                    :key="fund.id"
                    :fund="{
                        id: fund.id,
                        title: fund.title,
                        balance: fund.balance,
                        description: fund.description,
                        target: fund.target,
                        accountId: fund.accountId,
                        goalSummary: {
                            numberGoal: 4,
                            nextDueDate: new Date('08-14-2026')
                        }
                    }"
                    @add-goal=""
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

            <div v-if="funds.length === 0 && totalFund == 0" class="empty-state">
                <UIcon name="i-lucide-target" class="empty-icon" />
                <h3 class="empty-title">Aucun objectif d'épargne</h3>
                <p class="empty-description">Commencez par créer votre premier objectif</p>
                <UButton 
                    icon="i-lucide-plus" 
                    label="Créer un Objectif" 
                    size="xl"
                    @click="openModalFund()"
                />
            </div>
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

<style scoped lang="scss">
.goals-page {
    animation: fadeIn 0.4s ease;
}

// Summary Section (reusing budget styles)
.summary-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 1.25rem;
    margin-bottom: 2rem;

    @media (max-width: 768px) {
        grid-template-columns: repeat(2, 1fr);
        gap: 1rem;
    }

    @media (max-width: 480px) {
        grid-template-columns: 1fr;
    }
}

.summary-card {
    background: white;
    border-radius: 16px;
    padding: 1.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    border: 1px solid #f1f3f5;
    transition: all 0.3s ease;

    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
}

.summary-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1rem;
}

.summary-icon {
    width: 40px;
    height: 40px;
    padding: 0.5rem;
    background: rgba(103, 85, 215, 0.1);
    color: #6755d7;
    border-radius: 10px;

    &.saved {
        background: rgba(34, 197, 94, 0.1);
        color: #22c55e;
    }

    &.remaining {
        background: rgba(59, 130, 246, 0.1);
        color: #3b82f6;
    }

    &.progress {
        background: rgba(168, 85, 247, 0.1);
        color: #a855f7;
    }
}

.summary-label {
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 500;
}

.summary-amount {
    font-size: 2rem;
    font-weight: 800;
    color: #6755d7;
    letter-spacing: -0.5px;

    &.saved {
        color: #22c55e;
    }

    &.remaining {
        color: #3b82f6;
    }

    &.progress {
        color: #a855f7;
    }
}

// Action Bar
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    gap: 1rem;

    @media (max-width: 768px) {
        flex-direction: column;
        align-items: stretch;
    }
}

.view-toggle {
    display: flex;
    gap: 0.5rem;
    background: white;
    padding: 0.25rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.action-buttons {
    display: flex;
    gap: 0.75rem;

    @media (max-width: 640px) {
        flex-direction: column;
        width: 100%;
    }
}

.add-button {
    background: #6755d7 !important;
    color: white !important;
    font-weight: 600;

    &:hover {
        background: #5a47c7 !important;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(103, 85, 215, 0.3);
    }
}

// Goals Grid
.goals-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
    gap: 1.5rem;

    @media (max-width: 768px) {
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 1rem;
    }

    @media (max-width: 480px) {
        grid-template-columns: 1fr;
    }
}

.goal-card {
    background: white;
    border-radius: 16px;
    padding: 1.75rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    border: 1px solid #f1f3f5;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
        border-color: #e5e7eb;
    }
}

.goal-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1rem;
}

.goal-title-section {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.goal-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
    flex: 1;
}

.goal-percentage {
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    font-size: 0.875rem;
    font-weight: 700;
    background: rgba(103, 85, 215, 0.1);
    color: #6755d7;
}

.goal-actions {
    display: flex;
    gap: 0.5rem;
}

.goal-description {
    font-size: 0.875rem;
    color: #64748b;
    margin: 0 0 1.25rem 0;
    line-height: 1.5;
}

// Progress Section
.progress-section {
    margin-bottom: 1.25rem;
}

.progress-amounts {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
}

.amount-saved {
    font-size: 1.75rem;
    font-weight: 800;
    color: #22c55e;
    letter-spacing: -0.5px;
}

.amount-separator {
    font-size: 1.25rem;
    color: #cbd5e1;
    font-weight: 700;
}

.amount-target {
    font-size: 1.5rem;
    color: #6755d7;
    font-weight: 700;
}

.progress-remaining {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.75rem;
}

.remaining-label {
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 600;
}

.remaining-amount {
    font-size: 1.125rem;
    font-weight: 700;
    color: #3b82f6;
}

// Metadata
.goal-metadata {
    padding: 1rem 0;
    border-top: 1px solid #f1f3f5;
    border-bottom: 1px solid #f1f3f5;
    margin-bottom: 1rem;
}

.metadata-row {
    display: flex;
    gap: 1.5rem;
    margin-bottom: 0.75rem;
}

.metadata-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 500;
}

.rating-dots {
    display: flex;
    gap: 0.25rem;
}

.dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #e5e7eb;
    transition: all 0.2s ease;

    &.active {
        background: #fbbf24;
    }

    &.importance.active {
        background: #ef4444;
    }
}

.metadata-date {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: #94a3b8;
}

// Quick Actions
.quick-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
}

// Table Container
.table-container {
    background: white;
    border-radius: 16px;
    padding: 1.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    border: 1px solid #f1f3f5;
}

.table-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1.5rem;
    padding-top: 1.5rem;
    border-top: 1px solid #f1f3f5;

    @media (max-width: 640px) {
        flex-direction: column;
        gap: 1rem;
        align-items: stretch;
    }
}

.pagination-controls {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.pagination-label {
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 500;
}

.pagination-input {
    width: 80px;
}

// Delete Modal
.delete-modal {
    max-width: 28rem;
}

.modal-content {
    padding: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
}

.modal-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.modal-icon {
    width: 40px;
    height: 40px;
    padding: 0.5rem;
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    border-radius: 10px;
}

.modal-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
}

.modal-description {
    font-size: 0.95rem;
    color: #64748b;
    margin: 0;
    line-height: 1.5;
}

.modal-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
    margin-top: 0.5rem;
}

// Empty State
.empty-state {
    grid-column: 1 / -1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 2rem;
    text-align: center;
}

.empty-icon {
    width: 80px;
    height: 80px;
    color: #cbd5e1;
    margin-bottom: 1.5rem;
}

.empty-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: #475569;
    margin: 0 0 0.5rem 0;
}

.empty-description {
    font-size: 1rem;
    color: #94a3b8;
    margin: 0 0 2rem 0;
}

// Animations
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.goal-list-enter-active,
.goal-list-leave-active {
    transition: all 0.3s ease;
}

.goal-list-enter-from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
}

.goal-list-leave-to {
    opacity: 0;
    transform: scale(0.9);
}

.goal-list-move {
    transition: transform 0.3s ease;
}
</style>