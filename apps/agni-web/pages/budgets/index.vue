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

type BudgteItem = {
    id: string
    title: string    
    percentageSpend: number
    target: number
    balance: number
}

const {data: budgets, error, refresh} = useBudgets()
const displaybudgets = computed(() => {
    return budgets.value?.items.map(i => ({
        id: i.id,
        title: i.title,
        percentageSpend: roundNumber(computePercentage(i.target, i.currentBalance)),
        balance: i.currentBalance,
        target: i.target

    } satisfies BudgteItem))
})

const overlay = useOverlay();
const modalEditBudget = overlay.create(ModalEditBudget);
const toast = useToast();


const dateDisplayed = ref("Mois")
const listTypeDateDisplay = computed(() => (
[
    {
        label: 'Semaine' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            dateDisplayed.value = "Semain"
        } 
    },
    {
        label: 'Bi-Semaine' as const,
        type: "checkbox" as const,
        onSelect(e: Event) { 
            e.preventDefault()
            dateDisplayed.value = "Bi-Semain"
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
        label: 'Annee' as const,
        type: "checkbox" as const,
        onSelect(e: Event) {
            e.preventDefault()
            dateDisplayed.value = "Annee"
        } 
    }
]
))

async function onSubmitBudget(value: EditBudgetType, oldValue?: BudgetType) {
    try {
        if (oldValue)
            await useUpdateBudget(oldValue.id, {
                title: value.title,
                target: value.target,
                schedule: {
                    period: value.period,
                    periodTime: value.periodTime,
                    dateStart: value.startDate.toDate(getLocalTimeZone()).toISOString(),
                    dateEnd: value.endDate?.toDate(getLocalTimeZone()).toISOString()
                }
            })
        else 
            await useCreateBudget({
                title: value.title,
                target: value.target,
                schedule: {
                    period: value.period,
                    periodTime: value.periodTime,
                    dateStart: value.startDate.toDate(getLocalTimeZone()).toISOString(),
                    dateEnd: value.endDate?.toDate(getLocalTimeZone()).toISOString()
                }
            })
        refresh()
    } catch(err) {
        toast.add({
            title: 'Error budget',
            description: 'submition  budget error' + err,
            color: 'error'
        })
    }
}

async function openModalBudget(budgetId?: string) { 
    let budget: BudgetType|undefined;
    if (budgetId)
        budget = await fetchBudget(budgetId)

    modalEditBudget.open({
        budget: budget,
        onSubmit: onSubmitBudget
    }); 
}

const v = ref(50)

const onDeleteBudget = async (budgetId: string) => {
    await useDeleteBudget(budgetId)
    refresh();
}
</script>

<template>
    <div class="flex justify-between" style="margin-top: 1rem;">
        <div class="flex gap-2">
            <UDropdownMenu :items="listTypeDateDisplay">
                <UButton icon="i-lucide-calendar-days" size="xl" variant="outline" color="neutral" />
            </UDropdownMenu>
            <UButton  size="xl" variant="outline" color="neutral">
                {{ dateDisplayed }}
            </UButton>
        </div>
        <UButton icon="i-lucide-plus" label="Ajouter un budget" size="xl" @click="openModalBudget()" />
    </div> 

    <div style="margin-top: 1rem;">
        <div class="grid xs:grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-2">
            <div v-for="budget of displaybudgets" :key="budget.id">
                <div class="card-grid rounded-md" >
                    <CustomCardTitle :title="budget.title">
                        <div class="flex gap-1">
                            <UButton icon="i-lucide-pencil" variant="outline" color="neutral" size="xl" @click="openModalBudget(budget.id)"/>
                            <UButton icon="i-lucide-trash" variant="outline" color="neutral" size="xl" @click="onDeleteBudget(budget.id)"/>
                        </div>
                    </CustomCardTitle>
                    <div  style="margin-top: 1rem;">
                        <div>
                            <p class="text-sm" style="font-weight: 500;color: #D9D9D9;">Reste</p>
                            <div class="flex items-center">
                                <AmountTitle :amount="budget.target - budget.balance" /> 
                                <p class="text-2xl" style="font-weight: bold;color: #D9D9D9;">/</p>
                                <p style="color: #6755D7;">
                                    ${{ budget.target }}
                                </p>
                            </div>
                        </div>
                        
                    </div>
                    <div style="margin-top: 1rem;">
                        <UProgress status v-model="budget.percentageSpend"  ></UProgress>
                        <div class="flex items-center mt-2">
                            <p class="text-xl mr-2 font-semibold text-gray-500">Depense: </p>
                            <AmountTitle :amount="budget.balance" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</template>