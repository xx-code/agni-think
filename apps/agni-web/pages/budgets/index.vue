<script setup lang="ts">
import { ModalEditBudget } from "#components"
import { computed, ref } from "vue"
import { fetchBudget } from "~/composables/budgets/useBudget"
import useBudgets from "~/composables/budgets/useBudgets"
import useCreateBudget from "~/composables/budgets/useCreateBudget"
import useDeleteBudget from "~/composables/budgets/useDeleteBudget"
import useUpdateBudget from "~/composables/budgets/useUpdateBudget"
import type { BudgetType, EditBudgetType } from "~/types/ui/budget"

const {data: budgets, error, refresh} = useBudgets()

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
                    dateStart: value.startDate.toString(),
                    dateEnd: value.endDate?.toString()
                }
            })
        else 
            await useCreateBudget({
                title: value.title,
                target: value.target,
                schedule: {
                    period: value.period,
                    periodTime: value.periodTime,
                    dateStart: value.startDate.toString(),
                    dateEnd: value.endDate?.toString()
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
            <div v-for="budget of budgets?.items" :key="budget.id">
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
                                <AmountTitle :amount="budget.target - budget.currentBalance" /> 
                                <p class="text-2xl" style="font-weight: bold;color: #D9D9D9;">/</p>
                                <p style="color: #6755D7;">
                                    ${{ budget.target }}
                                </p>
                            </div>
                        </div>
                        
                    </div>
                    <div style="margin-top: 1rem;">
                        <UProgress v-model="budget.currentBalance" :max="budget.target" size="xl"/>
                        <div class="flex items-center mt-2">
                            <AmountTitle :amount="budget.currentBalance" />
                            <p style="font-weight: 500;color:#1E3050;" class="text-sm ml-2">| {{ ((budget.currentBalance/budget.target) * 100).toFixed(2) }}% Dépense</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</template>