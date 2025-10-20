<script setup lang="ts">
import { 
    ModalEditAmountSaveGoal, 
    ModalEditSaveGoal,
    ModalPlanningAdvisor, 
} from "#components";
import type { TableColumn, TableRow } from "#ui/types";
import { DateFormatter, getLocalTimeZone } from "@internationalized/date";
import { ref } from "vue";
import type { TargetGoal } from "~/components/Modal/PlanningAdvisor.vue";
import useAccounts from "~/composables/accounts/useAccounts";
import useCreateSaveGoal from "~/composables/goals/useCreateSaveGoal";
import useDeleteSaveGoal from "~/composables/goals/useDeleteSaveGoal";
import { fetchSaveGoal } from "~/composables/goals/useSaveGoal";
import useSaveGoals from "~/composables/goals/useSaveGoals";
import useUpdateAmountSaveGoal from "~/composables/goals/useUpdateAmountSaveGoal";
import useUpdateSaveGaol from "~/composables/goals/useUpdateSaveGoal";
import useImportanceTypes from "~/composables/internals/useImportanceTypes";
import useIntensityDesirTypes from "~/composables/internals/useImportanceTypes";
import type { queryFilterSaveGoalRequest } from "~/types/api/saveGoal";
import type { EditSaveGoalType, EditUpdateAmountSaveGoalType, SaveGoalType } from "~/types/ui/saveGoal";

type ItemRown = {
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
const filter = reactive<queryFilterSaveGoalRequest>({
    offset: 0,
    limit: 8,
    queryAll: false
});
const { data: goals, error: errorGoals, refresh: refreshGoals } =  useSaveGoals(filter)
const { data: accounts, error: errorAccount, refresh: refreshAccounts } = useAccounts({
    limit: 0,
    offset: 0,
    queryAll: true
})
const { data:intensityDesirs } = useIntensityDesirTypes();
const { data:importances } = useImportanceTypes()

const tableData = computed(() => {
    return goals.value?.items.map(i => {
        return {
            id: i.id,
            title: i.title,
            balance: i.balance,
            description: i.description,
            target: i.target,
            importance: i.importance,
            desir: i.desirValue,
            wishDate: i.wishDueDate
        } satisfies ItemRown
    })
})

const toast = useToast();
const overlay = useOverlay();
const modalCreateSavingGoal = overlay.create(ModalEditSaveGoal);
const modalUpdateAmountSavingGoal = overlay.create(ModalEditAmountSaveGoal);
const modalPlanningAdvisor = overlay.create(ModalPlanningAdvisor);

const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

async function onSubmitSaveGoal(value: EditSaveGoalType, oldValue?: SaveGoalType) {
    try {
        if (oldValue)
            useUpdateSaveGaol(oldValue.id, {
                title: value.title,
                description: value.description,
                target: value.target,
                importance: value.importance,
                desirValue: value.desirValue,
                wishDueDate: value.wishDueDate?.toDate(getLocalTimeZone()).toISOString()
            });
        else 
            useCreateSaveGoal({
                title: value.title,
                description: value.description,
                target: value.target,
                importance: value.importance,
                desirValue: value.desirValue,
                wishDueDate: value.wishDueDate?.toDate(getLocalTimeZone()).toISOString(),
                items: []
            });
        await refreshGoals();
    } catch(err) {
        toast.add({
            title: 'Error submition save goal',
            description: "Error while submit save goal " + err,
            color: 'error'
        })
    }
} 


async function openSavingGoal(goalId?: string) {
    let goal: SaveGoalType|undefined = undefined;
    if (goalId) {
        goal = await fetchSaveGoal(goalId); 
    }

    modalCreateSavingGoal.open({
        saveGoal: goal,
        onSubmit: onSubmitSaveGoal 
    }); 
};

async function onUpdateAmountSaveGoal(value: EditUpdateAmountSaveGoalType, isIncrease: boolean, oldValue?: SaveGoalType) {
    try {
        if (oldValue)
            await useUpdateAmountSaveGoal({
                isIncrease: isIncrease,
                amount: value.amount,
                accountId: value.accountId,
                saveGoalId: oldValue?.id
            }); 
    } catch(err) {
        toast.add({
            title: 'Error Update amount save goal',
            description: "Error while update save goal " + err,
            color: 'error'
        })
    }
}

const openUpdateAmountSavingGoal = async (isIncrease: boolean, goalId?: string) => {
    let goal: SaveGoalType|undefined = undefined;
    if (goalId) {
        goal = await fetchSaveGoal(goalId); 
    }
    
    modalUpdateAmountSavingGoal.open({
        isIncrease: isIncrease,
        onSubmit: onUpdateAmountSaveGoal,
        saveGoal: goal
    }); 

    refreshGoals()
};

const deleteAccountDepositId = ref('')
const deletePopOverOpen = ref(false)
const deletePopOverGoalId = ref<string>()

const onDeleteGoal = async (goalId: string) => {
    if (deleteAccountDepositId.value !== '') {
        await useDeleteSaveGoal(goalId, { accountDepositId: deleteAccountDepositId.value });
        await refreshGoals();
        deletePopOverOpen.value = false
        deletePopOverGoalId.value = undefined
    }
} 

const rowSelection = ref<Record<string, boolean>>({})
const table = useTemplateRef('table')

function onSelect(row: TableRow<ItemRown>, e?: Event) {
  row.toggleSelected(!row.getIsSelected())
}

function openPlanningAdvisorModal() {
    const selectedDataTable: TargetGoal[] = []
    table.value?.tableApi.getSelectedRowModel().rows.forEach(i => {
        selectedDataTable.push({ goalId: i.original.id, title: i.original.title })
    })

    modalPlanningAdvisor.open({
        targetGoals: selectedDataTable
    })
}

const UCheckbox = resolveComponent('UCheckbox')
const UProgress = resolveComponent('UProgress');
const UButton = resolveComponent('UButton');
const columns: TableColumn<ItemRown>[] =  [
    {
        id: 'select',
        header: ({ table }) =>
            h(UCheckbox, {
                modelValue: table.getIsSomePageRowsSelected()
                ? 'indeterminate'
                : table.getIsAllPageRowsSelected(),
                'onUpdate:modelValue': (value: boolean | 'indeterminate') =>
                table.toggleAllPageRowsSelected(!!value),
                'aria-label': 'Select all'
            })
        ,
        cell: ({ row }) =>
            h(UCheckbox, {
                modelValue: row.getIsSelected(),
                'onUpdate:modelValue': (value: boolean | 'indeterminate') => row.toggleSelected(!!value),
                'aria-label': 'Select row'
            })
    },
    {
        accessorKey: 'title',
        header: "Titre",
        cell: ({ row }) => 
            h('p', {
                class: "w-35 truncate"
            }, row.original.title)
    },
    {
        accessorKey: 'target',
        cell: ({ row }) => {
            return (
                h('div', {class: 'flex items-center'}, [
                    h('span', {class: 'mr-1 text-xs'}, 
                        formatCurrency(row.original.balance)
                    ),
                    h(UProgress, 
                        { 
                            modelValue: roundNumber(computePercentage(row.original.target, row.original.balance)) 
                        }
                    ),
                    h('span', {class: 'ml-1 text-xs'}, 
                        formatCurrency(row.original.target)
                    ),
                ])
            )
        }        
    },
    {
        id: 'left',
        header: "Reste",
        cell: ({ row }) => h('p', {}, formatCurrency(row.original.target - row.original.balance))
    },
    {
        accessorKey: 'desir',
        header: "Desire",
        cell: ({ row })  => {
            const desires = []
            const maxDesires = intensityDesirs.value?.length || row.original.desir
            for (let i = 0; i < maxDesires; i++)
                desires.push(h(UButton, { 
                icon: 'i-lucide-sparkles', 
                size: "xs",
                class: (i) < row.original.desir ? 'bg-yellow-400' : 'bg-white text-black',
                } ))

            return (
                h('div', { class: 'flex items-center space-x-1'},
                    desires
                )
            )
        }
    },
    {
        accessorKey: 'importance',
        header: "Importance",
        cell: ({ row })  => {
            const desires = []
            const maxDesires = importances.value?.length || row.original.importance
            for (let i = 0; i < maxDesires; i++)
                desires.push(h(UButton, { 
                icon: 'i-lucide-shield-alert', 
                size: "xs",
                class: (i) < row.original.importance ? 'bg-red-400' : 'bg-white text-black',
                } ))

            return (
                h('div', { class: 'flex items-center space-x-1'},
                    desires
                )
            )
        }
    },
    {
        accessorKey: 'wishDate',
        cell: ({ row }) => {
            return row.original.wishDate ? formatDate(row.original.wishDate) : ''
        }
    },
    {
        id: 'action',
        cell: ({ row }) => {
            return(
                h('div', { class: 'space-x-1'}, [
                    h(UButton, {icon:"i-lucide-plus", variant:"outline", color:"neutral", onClick:() => openUpdateAmountSavingGoal(true, row.original.id) }),
                    h(UButton, {icon:"i-lucide-minus", variant:"outline", color:"neutral",  onClick:() => openUpdateAmountSavingGoal(false, row.original.id) }),
                    h(UButton, {icon:"i-lucide-pencil", variant:"outline", color:"neutral", onClick:() => openSavingGoal(row.original.id)} ),
                    h(UButton, {icon:"i-lucide-trash", variant:"outline", color:"neutral",  onClick:() => { deletePopOverOpen.value = true, deletePopOverGoalId.value = row.original.id} }),
                ])
            )
        }
    }
]
                                

</script>

<template>
    <div>
       <div class="flex flex-row-reverse gap-1.5" style="margin-top: 1rem;">
            <UButton 
                icon="i-lucide-plus" 
                label="Ajouter un but" 
                @click="openSavingGoal()"/>
            <UButton 
                icon="i-lucide-bot" 
                color="primary"
                style="background-color: #6755D7;"
                label="Conseiller en planification" 
                @click="openPlanningAdvisorModal()"/>
       </div> 
       <div style="margin-top: 1rem;">
            <UTable 
                ref="table"
                v-model:row-selection="rowSelection"
                :columns="columns"
                :data="tableData" 
                @select="onSelect">
            </UTable>
            <div class="flex flex-row gap-2 items-baseline-last justify-between">
                <UPagination 
                    class="mt-3" 
                    v-model:page="page" 
                    v-on:update:page="(p) => filter.offset = (filter.limit * (p - 1))"
                    :items-per-page="filter.limit"  
                    :total="goals?.totals" 
                    active-variant="subtle" />
                <UInputNumber 
                    v-model="filter.limit" 
                    :min="1" 
                    orientation="vertical" 
                    style="width: 80px;"
                />
            </div>
        </div>
        <UModal v-model:open="deletePopOverOpen" class="w-50">
            <template #content>
                <div class="w-50 flex flex-col gap-3 p-2">
                    <USelect 
                        v-model="deleteAccountDepositId" 
                        value-key="value" label-key="label" 
                        :items="accounts?.items.map(i => ({ label: i.title, value: i.id}))" class="w-full" />
                    <UButton 
                        label="Suppmier" color="error" 
                        @click="
                            deletePopOverGoalId ? 
                                onDeleteGoal(deletePopOverGoalId) 
                                : 
                                () => {toast.add({title: 'Error', description: 'Selectionner un but'})} "/>
                </div>
            </template>
        </UModal>
    </div>    
    
</template>

<style lang="scss">
.card-grid {
    border: solid 1px #E6E6E6;
    padding: 0.5rem;
}
</style>