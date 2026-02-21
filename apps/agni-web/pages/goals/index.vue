<script setup lang="ts">
import { 
    ModalEditAmountSaveGoal, 
    ModalEditSaveGoal,
    ModalPlanningAdvisor, 
} from "#components"
import type { TableColumn, TableRow } from "#ui/types"
import { DateFormatter, getLocalTimeZone } from "@internationalized/date"
import { ref } from "vue"
import type { TargetGoal } from "~/components/Modal/PlanningAdvisor.vue"
import { fetchAccounts } from "~/composables/accounts/useAccounts"
import useCreateSaveGoal from "~/composables/goals/useCreateSaveGoal"
import useDeleteSaveGoal from "~/composables/goals/useDeleteSaveGoal"
import { fetchSaveGoal } from "~/composables/goals/useSaveGoal"
import { fetchSavingGoals } from "~/composables/goals/useSaveGoals"
import useUpdateAmountSaveGoal from "~/composables/goals/useUpdateAmountSaveGoal"
import useUpdateSaveGaol from "~/composables/goals/useUpdateSaveGoal"
import { fetchImportanceTypes } from "~/composables/internals/useImportanceTypes"
import { fetcheIntensityDesirTypes } from "~/composables/internals/useIntensityDerisTypes"
import type { QueryFilterSavingGoalRequest } from "~/types/api/saveGoal"
import type { EditSaveGoalType, EditUpdateAmountSaveGoalType, SaveGoalType } from "~/types/ui/saveGoal"

interface ItemRow  {
    id: string
    title: string
    description: string
    target: number 
    balance: number
    desir: number
    importance: number
    wishDate?: Date
}

const page = ref(1)
const filter = reactive<QueryFilterSavingGoalRequest>({
    offset: 0,
    limit: 8,
    queryAll: false
})

const { data: utils } = useAsyncData('utils+accounts', async () => {
    const query = { offset: 0, limit: 0, queryAll: true }
    const [ accounts, importances, intensityDesirs ] = await Promise.all([
        fetchAccounts(query),
        fetchImportanceTypes(),
        fetcheIntensityDesirTypes()
    ])

    return {
        accounts,
        importances,
        intensityDesirs
    }
})

const { data: goals, error, refresh } = useAsyncData('goal+page', async () => {
    const res = await fetchSavingGoals(filter)

    return res
}, {
    watch: [filter]
})  



const tableData = computed(() => {
    return goals.value?.items.map(i => ({
        id: i.id,
        title: i.title,
        balance: i.balance,
        description: i.description,
        target: i.target,
        importance: i.importance,
        desir: i.desirValue,
        wishDate: i.wishDueDate
    } satisfies ItemRow))
})

const toast = useToast()
const overlay = useOverlay()
const modalCreateSavingGoal = overlay.create(ModalEditSaveGoal)
const modalUpdateAmountSavingGoal = overlay.create(ModalEditAmountSaveGoal)
const modalPlanningAdvisor = overlay.create(ModalPlanningAdvisor)

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
})

// View mode toggle
const viewMode = ref<'cards' | 'table'>('cards')

// Computed summary stats
const totalTarget = computed(() => tableData.value?.reduce((sum, g) => sum + g.target, 0) || 0)
const totalSaved = computed(() => tableData.value?.reduce((sum, g) => sum + g.balance, 0) || 0)
const totalRemaining = computed(() => totalTarget.value - totalSaved.value)
const averageProgress = computed(() => {
    const data = tableData.value
    if (!data?.length) return 0
    return data.reduce((sum, g) => sum + (g.balance / g.target * 100), 0) / data.length
})

async function onSubmitSaveGoal(value: EditSaveGoalType, oldValue?: SaveGoalType) {
    try {
        if (oldValue) {
            await useUpdateSaveGaol(oldValue.id, {
                title: value.title,
                description: value.description,
                target: value.target,
                accountId: value.accountId,
                importance: value.importance,
                desirValue: value.desirValue,
                wishDueDate: value.wishDueDate?.toDate(getLocalTimeZone()).toISOString()
            })
        } else {
            await useCreateSaveGoal({
                title: value.title,
                description: value.description,
                target: value.target,
                accountId: value.accountId,
                importance: value.importance,
                desirValue: value.desirValue,
                wishDueDate: value.wishDueDate?.toDate(getLocalTimeZone()).toISOString(),
                items: []
            })
        }
        await refresh()
        toast.add({
            title: 'Succès',
            description: oldValue ? 'Objectif mis à jour' : 'Objectif créé',
            color: 'success'
        })
    } catch(err) {
        toast.add({
            title: 'Erreur',
            description: "Erreur lors de la soumission: " + err,
            color: 'error'
        })
    }
} 

async function openSavingGoal(goalId?: string) {
    let goal: SaveGoalType | undefined = undefined
    if (goalId) {
        goal = await fetchSaveGoal(goalId) 
    }


    modalCreateSavingGoal.open({
        saveGoal: goal,
        onSubmit: onSubmitSaveGoal 
    }) 
}

async function onUpdateAmountSaveGoal(value: EditUpdateAmountSaveGoalType, isIncrease: boolean, oldValue?: SaveGoalType) {
    try {
        if (oldValue) {
            await useUpdateAmountSaveGoal({
                isIncrease: isIncrease,
                amount: value.amount,
                accountId: value.accountId,
                saveGoalId: oldValue?.id
            })
            await refresh()
            toast.add({
                title: 'Succès',
                description: isIncrease ? 'Montant ajouté' : 'Montant retiré',
                color: 'success'
            })
        }
    } catch(err) {
        toast.add({
            title: 'Erreur',
            description: "Erreur lors de la mise à jour: " + err,
            color: 'error'
        })
    }
}

const openUpdateAmountSavingGoal = async (isIncrease: boolean, goalId?: string) => {
    let goal: SaveGoalType | undefined = undefined
    if (goalId) {
        goal = await fetchSaveGoal(goalId) 
    }
    
    modalUpdateAmountSavingGoal.open({
        isIncrease: isIncrease,
        onSubmit: onUpdateAmountSaveGoal,
        saveGoal: goal
    })
}

const deleteAccountDepositId = ref('')
const deletePopOverOpen = ref(false)
const deletePopOverGoalId = ref<string>()

const onDeleteGoal = async (goalId: string) => {
    if (deleteAccountDepositId.value !== '') {
        try {
            await useDeleteSaveGoal(goalId, { accountId: deleteAccountDepositId.value })
            await refresh()
            deletePopOverOpen.value = false
            deletePopOverGoalId.value = undefined
            toast.add({
                title: 'Succès',
                description: 'Objectif supprimé',
                color: 'success'
            })
        } catch(err) {
            toast.add({
                title: 'Erreur',
                description: 'Impossible de supprimer l\'objectif',
                color: 'error'
            })
        }
    } else {
        toast.add({
            title: 'Erreur',
            description: 'Veuillez sélectionner un compte de dépôt',
            color: 'error'
        })
    }
} 

const rowSelection = ref<Record<string, boolean>>({})
const table = useTemplateRef('table')

function onSelectItem(row: TableRow<ItemRow>) {
  row.toggleSelected(!row.getIsSelected())
}

function openPlanningAdvisorModal() {
    const selectedDataTable: TargetGoal[] = []
    table.value?.tableApi.getSelectedRowModel().rows.forEach(i => {
        selectedDataTable.push({ goalId: i.original.id, title: i.original.title })
    })

    if (selectedDataTable.length === 0) {
        toast.add({
            title: 'Attention',
            description: 'Veuillez sélectionner au moins un objectif',
            color: 'warning'
        })
        return
    }

    modalPlanningAdvisor.open({
        targetGoals: selectedDataTable
    })
}

const UCheckbox = resolveComponent('UCheckbox')
const UProgress = resolveComponent('UProgress')
const UButton = resolveComponent('UButton')

const columns: TableColumn<ItemRow>[] = [
    {
        id: 'select',
        header: ({ table }) =>
            h(UCheckbox, {
                modelValue: table.getIsSomePageRowsSelected()
                ? 'indeterminate'
                : table.getIsAllPageRowsSelected(),
                'onUpdate:modelValue': (value: boolean | 'indeterminate') =>
                table.toggleAllPageRowsSelected(!!value),
                'aria-label': 'Tout sélectionner'
            }),
        cell: ({ row }) =>
            h(UCheckbox, {
                modelValue: row.getIsSelected(),
                'onUpdate:modelValue': (value: boolean | 'indeterminate') => row.toggleSelected(!!value),
                'aria-label': 'Sélectionner la ligne'
            })
    },
    {
        accessorKey: 'title',
        header: "Titre",
        cell: ({ row }) => 
            h('div', { class: 'flex flex-col' }, [
                h('p', { class: 'font-semibold text-gray-900 truncate max-w-48' }, row.original.title),
                h('p', { class: 'text-xs text-gray-500 truncate max-w-48' }, row.original.description)
            ])
    },
    {
        accessorKey: 'target',
        header: "Progression",
        cell: ({ row }) => {
            const percentage = roundNumber(computePercentage(row.original.target, row.original.balance))
            return h('div', { class: 'flex flex-col gap-1 min-w-48' }, [
                h('div', { class: 'flex items-center gap-2' }, [
                    h('span', { class: 'text-sm font-semibold text-gray-900' }, 
                        formatCurrency(row.original.balance)
                    ),
                    h('span', { class: 'text-xs text-gray-400' }, '/'),
                    h('span', { class: 'text-sm text-gray-600' }, 
                        formatCurrency(row.original.target)
                    ),
                ]),
                h(UProgress, { 
                    modelValue: percentage,
                    color: percentage >= 90 ? 'success' : percentage >= 50 ? 'primary' : 'neutral'
                })
            ])
        }        
    },
    {
        id: 'left',
        header: "Restant",
        cell: ({ row }) => h('p', { class: 'font-semibold text-blue-600' }, 
            formatCurrency(row.original.target - row.original.balance)
        )
    },
    {
        accessorKey: 'desir',
        header: "Désir",
        cell: ({ row }) => {
            const desires = []
            const maxDesires = utils.value?.intensityDesirs.length || row.original.desir
            for (let i = 0; i < maxDesires; i++)
                desires.push(h(UButton, { 
                    icon: 'i-lucide-sparkles', 
                    size: "xs",
                    class: (i) < row.original.desir ? 'bg-yellow-400 hover:bg-yellow-500' : 'bg-gray-100 text-gray-400',
                }))

            return h('div', { class: 'flex items-center gap-1' }, desires)
        }
    },
    {
        accessorKey: 'importance',
        header: "Importance",
        cell: ({ row }) => {
            const desires = []
            const maxDesires = utils.value?.importances.length || row.original.importance
            for (let i = 0; i < maxDesires; i++)
                desires.push(h(UButton, { 
                    icon: 'i-lucide-shield-alert', 
                    size: "xs",
                    class: (i) < row.original.importance ? 'bg-red-400 hover:bg-red-500' : 'bg-gray-100 text-gray-400',
                }))

            return h('div', { class: 'flex items-center gap-1' }, desires)
        }
    },
    {
        accessorKey: 'wishDate',
        header: "Date souhaitée",
        cell: ({ row }) => {
            return row.original.wishDate 
                ? h('span', { class: 'text-sm text-gray-600' }, formatDate(row.original.wishDate))
                : h('span', { class: 'text-sm text-gray-400' }, '—')
        }
    },
    {
        id: 'action',
        header: "Actions",
        cell: ({ row }) => {
            return h('div', { class: 'flex gap-1' }, [
                h(UButton, {
                    icon: "i-lucide-plus",
                    variant: "ghost",
                    color: "success",
                    size: "sm",
                    onClick: () => openUpdateAmountSavingGoal(true, row.original.id)
                }),
                h(UButton, {
                    icon: "i-lucide-minus",
                    variant: "ghost",
                    color: "warning",
                    size: "sm",
                    onClick: () => openUpdateAmountSavingGoal(false, row.original.id)
                }),
                h(UButton, {
                    icon: "i-lucide-pencil",
                    variant: "ghost",
                    color: "neutral",
                    size: "sm",
                    onClick: () => openSavingGoal(row.original.id)
                }),
                h(UButton, {
                    icon: "i-lucide-trash-2",
                    variant: "ghost",
                    color: "error",
                    size: "sm",
                    onClick: () => {
                        deletePopOverOpen.value = true
                        deletePopOverGoalId.value = row.original.id
                    }
                }),
            ])
        }
    }
]
</script>

<template>
    <div class="goals-page">
        <!-- Summary Section -->
        <div class="summary-section">
            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-target" class="summary-icon" />
                    <span class="summary-label">Objectif Total</span>
                </div>
                <div class="summary-amount">${{ totalTarget.toLocaleString() }}</div>
            </div>

            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-piggy-bank" class="summary-icon saved" />
                    <span class="summary-label">Économisé</span>
                </div>
                <div class="summary-amount saved">${{ totalSaved.toLocaleString() }}</div>
            </div>

            <div class="summary-card">
                <div class="summary-header">
                    <UIcon name="i-lucide-trending-up" class="summary-icon remaining" />
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
            <div class="view-toggle">
                <UButton
                    :icon="viewMode === 'cards' ? 'i-lucide-layout-grid' : 'i-lucide-layout-grid'"
                    :variant="viewMode === 'cards' ? 'solid' : 'outline'"
                    color="neutral"
                    size="lg"
                    @click="viewMode = 'cards'"
                />
                <UButton
                    :icon="viewMode === 'table' ? 'i-lucide-table-2' : 'i-lucide-table-2'"
                    :variant="viewMode === 'table' ? 'solid' : 'outline'"
                    color="neutral"
                    size="lg"
                    @click="viewMode = 'table'"
                />
            </div>

            <div class="action-buttons">
                <UButton 
                    icon="i-lucide-bot" 
                    label="Conseiller en Planification"
                    size="xl"
                    variant="outline"
                    color="primary"
                    @click="openPlanningAdvisorModal()"
                />
                <UButton 
                    icon="i-lucide-plus" 
                    label="Nouvel Objectif" 
                    size="xl"
                    class="add-button"
                    @click="openSavingGoal()"
                />
            </div>
        </div>

        <!-- Cards View -->
        <div v-if="viewMode === 'cards'" class="goals-grid">
            <TransitionGroup name="goal-list">
                <div 
                    v-for="goal in tableData" 
                    :key="goal.id"
                    class="goal-card"
                >
                    <!-- Card Header -->
                    <div class="goal-header">
                        <div class="goal-title-section">
                            <h3 class="goal-title">{{ goal.title }}</h3>
                            <span class="goal-percentage">
                                {{ roundNumber(computePercentage(goal.target, goal.balance)) }}%
                            </span>
                        </div>
                        
                        <div class="goal-actions">
                            <UButton 
                                icon="i-lucide-pencil" 
                                variant="ghost" 
                                color="neutral" 
                                size="sm"
                                @click="openSavingGoal(goal.id)"
                            />
                            <UButton 
                                icon="i-lucide-trash-2" 
                                variant="ghost" 
                                color="error" 
                                size="sm"
                                @click="() => {
                                    deletePopOverOpen = true
                                    deletePopOverGoalId = goal.id
                                }"
                            />
                        </div>
                    </div>

                    <!-- Description -->
                    <p v-if="goal.description" class="goal-description">{{ goal.description }}</p>

                    <!-- Progress Section -->
                    <div class="progress-section">
                        <div class="progress-amounts">
                            <span class="amount-saved">${{ goal.balance.toLocaleString() }}</span>
                            <span class="amount-separator">/</span>
                            <span class="amount-target">${{ goal.target.toLocaleString() }}</span>
                        </div>
                        <UProgress 
                            v-bind:model-value="roundNumber(computePercentage(goal.target, goal.balance))" 
                            :color="computePercentage(goal.target, goal.balance) >= 90 ? 'success' : 'primary'"
                            size="md"
                        />
                        <div class="progress-remaining">
                            <span class="remaining-label">Restant</span>
                            <span class="remaining-amount">${{ (goal.target - goal.balance).toLocaleString() }}</span>
                        </div>
                    </div>

                    <!-- Metadata -->
                    <div class="goal-metadata">
                        <div class="metadata-row">
                            <div class="metadata-item">
                                <UIcon name="i-lucide-sparkles" size="sm" />
                                <span>Désir</span>
                                <div class="rating-dots">
                                    <span 
                                        v-for="i in (utils?.intensityDesirs.length || 5)"
                                        :key="i"
                                        class="dot"
                                        :class="{ active: i <= goal.desir }"
                                    />
                                </div>
                            </div>
                            
                            <div class="metadata-item">
                                <UIcon name="i-lucide-shield-alert" size="sm" />
                                <span>Importance</span>
                                <div class="rating-dots">
                                    <span 
                                        v-for="i in (utils?.importances.length || 5)"
                                        :key="i"
                                        class="dot importance"
                                        :class="{ active: i <= goal.importance }"
                                    />
                                </div>
                            </div>
                        </div>

                        <div v-if="goal.wishDate" class="metadata-date">
                            <UIcon name="i-lucide-calendar-clock" size="sm" />
                            <span>{{ formatDate(goal.wishDate) }}</span>
                        </div>
                    </div>

                    <!-- Quick Actions -->
                    <div class="quick-actions">
                        <UButton 
                            icon="i-lucide-plus"
                            label="Ajouter"
                            variant="outline"
                            color="success"
                            block
                            @click="openUpdateAmountSavingGoal(true, goal.id)"
                        />
                        <UButton 
                            icon="i-lucide-minus"
                            label="Retirer"
                            variant="outline"
                            color="warning"
                            block
                            @click="openUpdateAmountSavingGoal(false, goal.id)"
                        />
                    </div>
                </div>
            </TransitionGroup>

            <!-- Empty State -->
            <div v-if="!tableData?.length" class="empty-state">
                <UIcon name="i-lucide-target" class="empty-icon" />
                <h3 class="empty-title">Aucun objectif d'épargne</h3>
                <p class="empty-description">Commencez par créer votre premier objectif</p>
                <UButton 
                    icon="i-lucide-plus" 
                    label="Créer un Objectif" 
                    size="xl"
                    @click="openSavingGoal()"
                />
            </div>
        </div>

        <!-- Table View -->
        <div v-else class="table-container">
            <UTable 
                ref="table"
                v-model:row-selection="rowSelection"
                :columns="columns"
                :data="tableData" 
                @select="(row, e) => onSelectItem(row as any)"
            />
            <div class="table-footer">
                <UPagination 
                    v-model:page="page" 
                    @update:page="() => filter.offset = (filter.limit * (page - 1))"
                    :items-per-page="filter.limit"  
                    :total="goals?.total" 
                    active-variant="subtle"
                />
                <div class="pagination-controls">
                    <span class="pagination-label">Lignes par page</span>
                    <UInputNumber 
                        v-model="filter.limit" 
                        :min="1" 
                        :max="50"
                        orientation="vertical"
                        class="pagination-input"
                    />
                </div>
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
                        :items="utils?.accounts.items.map(i => ({ label: i.title, value: i.id }))" 
                        size="xl"
                    />
                    <div class="modal-actions">
                        <UButton 
                            label="Annuler" 
                            variant="outline"
                            color="neutral"
                            block
                            @click="deletePopOverOpen = false"
                        />
                        <UButton 
                            label="Supprimer" 
                            color="error"
                            block
                            @click="deletePopOverGoalId ? onDeleteGoal(deletePopOverGoalId) : null"
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