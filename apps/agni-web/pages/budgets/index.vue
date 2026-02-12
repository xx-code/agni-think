<script setup lang="ts">
import { ModalEditBudget } from "#components"
import { getLocalTimeZone } from "@internationalized/date"
import { computed, ref } from "vue"
import { fetchBudget } from "~/composables/budgets/useBudget"
import useBudgets from "~/composables/budgets/useBudgets"
import useCreateBudget from "~/composables/budgets/useCreateBudget"
import useDeleteBudget from "~/composables/budgets/useDeleteBudget"
import useUpdateBudget from "~/composables/budgets/useUpdateBudget"
import type { BudgetType, EditBudgetType } from "~/types/ui/budget"

type BudgetItem = {
    id: string
    title: string    
    percentageSpend: number
    target: number
    realTarget: number
    saveTarget: number
    balance: number
    dueDate: Date
}

const { data: budgets, error, refresh } = useBudgets({
    limit: 0,
    offset: 0,
    queryAll: true
})

const displayBudgets = computed(() => {
    return budgets.value?.items.map(i => ({
        id: i.id,
        title: i.title,
        percentageSpend: roundNumber(computePercentage(i.target, i.currentBalance)),
        balance: i.currentBalance,
        target: i.target,
        realTarget: i.realTarget,
        saveTarget: i.saveGoalTarget,
        dueDate: i.dueDate
    } satisfies BudgetItem))
})

const overlay = useOverlay()
const modalEditBudget = overlay.create(ModalEditBudget)
const toast = useToast()

const dateDisplayed = ref("Mois")
const listTypeDateDisplay = computed(() => ([
    {
        label: 'Semaine' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            dateDisplayed.value = "Semaine"
        } 
    },
    {
        label: 'Bi-Semaine' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            dateDisplayed.value = "Bi-Semaine"
        } 
    },
    {
        label: 'Mois' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Mois"
        } 
    },
    {
        label: 'Trimestre' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Trimestre"
        } 
    },
    {
        label: 'Semestre' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Semestre"
        } 
    },
    {
        label: 'Année' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Année"
        } 
    }
]))

async function onSubmitBudget(value: EditBudgetType, oldValue?: BudgetType) {
    try {
        if (oldValue)
            await useUpdateBudget(oldValue.id, {
                title: value.title,
                target: value.target,
                saveGoalIds: value.saveGoalIds,
                schedule: {
                    repeater: value.repeater,
                    dueDate: value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                }
            })
        else 
            await useCreateBudget({
                title: value.title,
                target: value.target,
                savingGoalIds: value.saveGoalIds,
                schedule: {
                    repeater: value.repeater,
                    dueDate: value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                }
            })
        refresh()
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
        budget = await fetchBudget(budgetId)
    }

    modalEditBudget.open({
        budget: budget,
        onSubmit: onSubmitBudget
    }) 
}

const onDeleteBudget = async (budgetId: string) => {
    try {
        await useDeleteBudget(budgetId)
        refresh()
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

// Computed summary stats
const totalBudget = computed(() => displayBudgets.value?.reduce((sum, b) => sum + b.target, 0) || 0)
const totalSpent = computed(() => displayBudgets.value?.reduce((sum, b) => sum + b.balance, 0) || 0)
const totalRemaining = computed(() => totalBudget.value - totalSpent.value)
const averageProgress = computed(() => {
    const budgets = displayBudgets.value
    if (!budgets?.length) return 0
    return budgets.reduce((sum, b) => sum + b.percentageSpend, 0) / budgets.length
})

// Helper to get status color
const getStatusColor = (percentage: number) => {
    if (percentage >= 90) return 'error'
    if (percentage >= 70) return 'warning'
    return 'success'
}
</script>

<template>
    <div class="budget-page">
        <!-- Summary Cards -->
        <div class="summary-section">
            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-wallet" class="summary-icon" />
                    <span class="summary-label">Budget Total</span>
                </div>
                <div class="summary-amount">${{ totalBudget.toLocaleString() }}</div>
            </div>

            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-trending-up" class="summary-icon spend" />
                    <span class="summary-label">Dépensé</span>
                </div>
                <div class="summary-amount spend">${{ totalSpent.toLocaleString() }}</div>
            </div>

            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-piggy-bank" class="summary-icon remaining" />
                    <span class="summary-label">Restant</span>
                </div>
                <div class="summary-amount remaining">${{ totalRemaining.toLocaleString() }}</div>
            </div>

            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-activity" class="summary-icon progress" />
                    <span class="summary-label">Progression Moyenne</span>
                </div>
                <div class="summary-amount progress">{{ averageProgress.toFixed(1) }}%</div>
            </div>
        </div>

        <!-- Action Bar -->
        <div class="action-bar">
            <div class="period-selector">
                <UDropdownMenu :items="listTypeDateDisplay">
                    <UButton 
                        icon="i-lucide-calendar-days" 
                        size="xl" 
                        variant="outline" 
                        color="neutral"
                        class="period-button"
                    />
                </UDropdownMenu>
                <div class="period-display">
                    <UIcon name="i-lucide-calendar" size="sm" />
                    <span>{{ dateDisplayed }}</span>
                </div>
            </div>

            <UButton 
                icon="i-lucide-plus" 
                label="Nouveau Budget" 
                size="xl" 
                class="add-button"
                @click="openModalBudget()"
            />
        </div>

        <!-- Budget Grid -->
        <div class="budget-grid">
            <TransitionGroup name="budget-list">
                <div 
                    v-for="budget in displayBudgets" 
                    :key="budget.id"
                    class="budget-card"
                >
                    <!-- Card Header -->
                    <div class="budget-header">
                        <div class="budget-title-section">
                            <h3 class="budget-title">{{ budget.title }}</h3>
                            <span 
                                class="budget-status-badge"
                                :class="getStatusColor(budget.percentageSpend)"
                            >
                                {{ budget.percentageSpend }}%
                            </span>
                        </div>
                        
                        <div class="budget-actions">
                            <UButton 
                                icon="i-lucide-pencil" 
                                variant="ghost" 
                                color="neutral" 
                                size="sm"
                                @click="openModalBudget(budget.id)"
                            />
                            <UButton 
                                icon="i-lucide-trash-2" 
                                variant="ghost" 
                                color="error" 
                                size="sm"
                                @click="onDeleteBudget(budget.id)"
                            />
                        </div>
                    </div>

                    <!-- Budget Amount Section -->
                    <div class="budget-amounts">
                        <div class="amount-row">
                            <div class="amount-main">
                                <span class="amount-label">Restant</span>
                                <div class="amount-display">
                                    <span class="amount-value remaining">
                                        ${{ (budget.target - budget.balance).toLocaleString() }}
                                    </span>
                                    <span class="amount-separator">/</span>
                                    <span class="amount-total">
                                        ${{ budget.target.toLocaleString() }}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div class="amount-breakdown">
                            <div class="breakdown-item">
                                <UIcon name="i-lucide-coins" size="sm" />
                                <span>Réel: ${{ budget.realTarget.toLocaleString() }}</span>
                            </div>
                            <div class="breakdown-item">
                                <UIcon name="i-lucide-target" size="sm" />
                                <span>Objectif: ${{ budget.saveTarget.toLocaleString() }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- Progress Bar -->
                    <div class="progress-section">
                        <UProgress 
                            v-model="budget.percentageSpend" 
                            :color="getStatusColor(budget.percentageSpend)"
                            size="md"
                        />
                        <div class="progress-details">
                            <div class="progress-label">
                                <UIcon name="i-lucide-credit-card" size="sm" />
                                <span>Dépensé</span>
                            </div>
                            <span class="progress-amount">
                                ${{ budget.balance.toLocaleString() }}
                            </span>
                        </div>
                    </div>

                    <!-- Card Footer -->
                    <div class="budget-footer">
                        <div class="footer-item">
                            <UIcon name="i-lucide-calendar-clock" size="sm" />
                            <span>{{ formatDate(budget.dueDate) }}</span>
                        </div>
                    </div>
                </div>
            </TransitionGroup>

            <!-- Empty State -->
            <div v-if="!displayBudgets?.length" class="empty-state">
                <UIcon name="i-lucide-wallet-cards" class="empty-icon" />
                <h3 class="empty-title">Aucun budget</h3>
                <p class="empty-description">Commencez par créer votre premier budget</p>
                <UButton 
                    icon="i-lucide-plus" 
                    label="Créer un Budget" 
                    size="xl"
                    @click="openModalBudget()"
                />
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.budget-page {
    animation: fadeIn 0.4s ease;
}

// Summary Section
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

    &.spend {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    &.remaining {
        background: rgba(34, 197, 94, 0.1);
        color: #22c55e;
    }

    &.progress {
        background: rgba(59, 130, 246, 0.1);
        color: #3b82f6;
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

    &.spend {
        color: #ef4444;
    }

    &.remaining {
        color: #22c55e;
    }

    &.progress {
        color: #3b82f6;
    }
}

// Action Bar
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    gap: 1rem;

    @media (max-width: 640px) {
        flex-direction: column;
        align-items: stretch;
    }
}

.period-selector {
    display: flex;
    gap: 0.75rem;
    align-items: center;
}

.period-display {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.25rem;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    font-weight: 600;
    color: #1e293b;
    font-size: 0.95rem;
}

.add-button {
    background: #6755d7 !important;
    color: white !important;
    font-weight: 600;
    transition: all 0.2s ease;

    &:hover {
        background: #5a47c7 !important;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(103, 85, 215, 0.3);
    }
}

// Budget Grid
.budget-grid {
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

.budget-card {
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

// Budget Card Header
.budget-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 1.5rem;
}

.budget-title-section {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.budget-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
}

.budget-status-badge {
    padding: 0.25rem 0.75rem;
    border-radius: 12px;
    font-size: 0.875rem;
    font-weight: 700;

    &.success {
        background: rgba(34, 197, 94, 0.1);
        color: #22c55e;
    }

    &.warning {
        background: rgba(251, 146, 60, 0.1);
        color: #fb923c;
    }

    &.error {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }
}

.budget-actions {
    display: flex;
    gap: 0.5rem;
}

// Budget Amounts
.budget-amounts {
    margin-bottom: 1.5rem;
}

.amount-row {
    margin-bottom: 1rem;
}

.amount-label {
    display: block;
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 600;
    margin-bottom: 0.5rem;
}

.amount-display {
    display: flex;
    align-items: baseline;
    gap: 0.5rem;
}

.amount-value {
    font-size: 2rem;
    font-weight: 800;
    letter-spacing: -0.5px;

    &.remaining {
        color: #22c55e;
    }
}

.amount-separator {
    font-size: 1.5rem;
    color: #cbd5e1;
    font-weight: 700;
}

.amount-total {
    font-size: 1.5rem;
    color: #6755d7;
    font-weight: 700;
}

.amount-breakdown {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
}

.breakdown-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    background: #f8f9fa;
    border-radius: 8px;
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 500;
}

// Progress Section
.progress-section {
    margin-bottom: 1.25rem;
}

.progress-details {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.75rem;
}

.progress-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 600;
}

.progress-amount {
    font-size: 1.125rem;
    font-weight: 700;
    color: #1e293b;
}

// Budget Footer
.budget-footer {
    padding-top: 1rem;
    border-top: 1px solid #f1f3f5;
}

.footer-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: #94a3b8;
    font-weight: 500;
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

.budget-list-enter-active,
.budget-list-leave-active {
    transition: all 0.3s ease;
}

.budget-list-enter-from {
    opacity: 0;
    transform: scale(0.9) translateY(20px);
}

.budget-list-leave-to {
    opacity: 0;
    transform: scale(0.9);
}

.budget-list-move {
    transition: transform 0.3s ease;
}
</style>