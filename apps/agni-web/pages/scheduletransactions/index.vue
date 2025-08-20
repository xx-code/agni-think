<script setup lang="ts">
import { ModalEditScheduleTransaction } from '#components';
import type { TableColumn, TableRow } from '#ui/types';
import { getLocalTimeZone } from '@internationalized/date';
import useCategories from '~/composables/categories/useCategories';
import useCreateScheduleTransaction from '~/composables/scheduleTransactions/useCreateScheduleTransaction';
import useDeleteScheduleTransaction from '~/composables/scheduleTransactions/useDeleteScheduleTransaction';
import { fetchScheduleTransaction } from '~/composables/scheduleTransactions/useScheduleTransaction';
import useScheduleTransactions from '~/composables/scheduleTransactions/useScheduleTransactions';
import useUpdateScheduleTransaction from '~/composables/scheduleTransactions/useUpdateScheduleTransaction';
import useTags from '~/composables/tags/useTags';
import type { EditScheduleTransactionType, ScheduleTransactionType, TableScheduleTransactionType } from '~/types/ui/scheduleTransaction';
import { parseAsLocalDate } from '~/utils/parseAsLocalDate';

const {data: categories, error: errorCategory, refresh: refreshCategory } = useCategories();
const {data: tags, error: errorTag, refresh: refreshTag } = useTags();
const toast = useToast();

const getCategory = (id: string) => categories.value?.items.find(i => id === i.id)
const getTag = (id: string) => tags.value?.items.find(i => id === i.id)
const {data: scheduleTransactions, error: errorTransactions, refresh: refreshScheduleTransactions } = useScheduleTransactions();
const displayScheluletransactionsTable = computed(() => {
    return scheduleTransactions.value?.items.map(i => ({
        id: i.id,
        amount: i.amount,
        dateStart: i.dateStart,
        dateUpdate: i.dateUpdate,
        dateEnd: i.dateEnd,
        name: i.name,
        type: i.type,
        isPause: i.isPause,
        isFreeze: i.isFreeze,
        category: {
            id: i.categoryId,
            icon: getCategory(i.categoryId)?.icon || '',
            color: getCategory(i.categoryId)?.color || '',
            title: getCategory(i.categoryId)?.title || '',
        },
        tags: i.tagIds.map(i => ({
            id: i,
            value: getTag(i)?.value || '',
            color: getTag(i)?.color || ''
        })) 
    } satisfies TableScheduleTransactionType))    
})

async function togglePauseSchedule(id:string, isPause: boolean) {
    await useUpdateScheduleTransaction(id, {
        isPause: !isPause
    });

    refreshScheduleTransactions();
}

const UIcon = resolveComponent('UIcon');
const UButton = resolveComponent('UButton');
const UDropdownMenu = resolveComponent('UDropdownMenu');
const UBadge = resolveComponent('UBadge');
const tableColumn: TableColumn<TableScheduleTransactionType>[] = [
    {
        id: 'expand',
        cell: ({ row }) => {
            const tags = row.original.tags
            if (tags.length > 0)
                return (
                    h(UButton, {
                        color: 'neutral',
                        variant: 'ghost',
                        icon: 'i-lucide-chevron-down',
                        square: true,
                        'aria-label': 'Expand',
                        ui: {
                            leadingIcon: [
                                'transition-transform',
                                row.getIsExpanded() ? 'duration-200 rotate-180': ''
                            ]
                        },
                        onClick: () => row.toggleExpanded()
                    })
                )
        }
    },
    {
        id: 'icon',
        header: '',
        cell: ({ row }) => {
            const icon = row.original.category.icon
            const color = row.original.category.color
            return(
                h('div', {
                    class: 'flex items-center justify-center rounded-full',
                    style: {
                        background: `${color}22`,
                 
                        width: '35px',
                        height: '35px',
                    }
                }, [
                    h(UIcon, { name: icon, class: 'text-white text-lg', style:{color: color}  }) // ou une autre couleur si tu veux
                ])
            )
        }
    },
    {
        accessorKey: 'dateStart',
        header: 'Date Debut',
        cell: ({ row }) => {
            return parseAsLocalDate(row.getValue('dateStart')).toLocaleString('fr-Fr', {
                day: 'numeric',
                month: 'short',
                year: '2-digit'
            })
        }
    },
    {
        accessorKey: 'dateUpdate',
        header: 'Date Mise a jour',
        cell: ({ row }) => {
            return parseAsLocalDate(row.getValue('dateUpdate')).toLocaleString('fr-Fr', {
                day: 'numeric',
                month: 'short',
                year: '2-digit'
            })
        }
    },
    {
        accessorKey: 'dateEnd',
        header: 'Date de fin',
        cell: ({ row }) => {
            if (row.getValue('dateEnd') === undefined)
                return '';

            return parseAsLocalDate(row.getValue('dateEnd')).toLocaleString('fr-Fr', {
                day: 'numeric',
                month: 'short',
                hour: '2-digit',
                minute: '2-digit',
                hour12: false,
                timeZone: 'UTC'
            });
        }
    },
    {
        accessorKey: 'category.title',
        header: 'Categorie',
    },
    {
        accessorKey: 'name',
        header: 'Nom',
    }, 
    {
        accessorKey: 'isPause',
        header: '',
        cell: ({ row }) => {
            if (!row.original.isPause)
                return h(UButton, { 
                    icon: 'i-lucide-pause', 
                    size: 'md', 
                    class: 'rounded-full',
                    variant: 'outline',
                    onClick: () => togglePauseSchedule(row.original.id, row.original.isPause)
                 })

            return h(UButton, { 
                    icon: 'i-lucide-play', 
                    size: 'md', 
                    class: 'rounded-full',
                    variant: 'outline',
                    onClick: () => togglePauseSchedule(row.original.id, row.original.isPause)
                 })
        }
    },
    {
        accessorKey: 'amount',
        header: () => h('div', { class: 'text-right' }, 'Amount'),
        cell: ({ row }) => {
            const amount = Number.parseFloat(row.getValue('amount'))
            const type = row.original.type
            

            const formatted = new Intl.NumberFormat('en-US', {
                style: 'currency',
                currency: 'CAD'
            }).format(amount)

            return h('div', {class: 'text-right font-medium', style: {color: type === 'Income' ? "#1abc9c" : ""}}, formatted)
            
        }
    },
    {
        id: 'action',
        cell: ({ row }) => {
            return h(
                'div',
                {  class: 'text-right'},
                h(
                    UDropdownMenu,
                    {
                        content: {
                            align: 'end'
                        },
                        items: getRowItems(row),
                        'arial-label': 'Actions dropdown'
                    },
                    () => 
                        h(
                            UButton, {
                                icon: 'i-lucide-ellipsis-vertical',
                                color: 'neutral',
                                variant: 'ghost',
                                class: 'ml-auto',
                                'aria-label': 'Actions dropdown'
                            }
                        )
                )
            )
        }
    }
]

function getRowItems(rows: TableRow<TableScheduleTransactionType>) {
    return [
        {
            label: 'Modifier',
            disabled: rows.original.isFreeze,
            onSelect: () => {
                const id = rows.original.id
                openTransaction(id)
            }
        },
        {
            label: 'Supprimer',
            onSelect: () => {
                const id = rows.original.id
                if (confirm("Voulez vous supprimer la transaction"))
                    onDelete(id)
            }
        }
    ] 
}

const overlay = useOverlay()
const modalScheduleTransaction = overlay.create(ModalEditScheduleTransaction);

async function onSubmitTransaction(value: EditScheduleTransactionType, oldValue?: ScheduleTransactionType) {
    try {
        if (oldValue)
            await useUpdateScheduleTransaction(oldValue.id, {
                accountId: value.accountId,
                amount: value.amount,
                categoryId: value.categoryId,
                schedule:{
                    dateStart:  value.dateStart.toString(),
                    dateEnd: value.dateEnd?.toString(),
                    period: value.period,
                    periodTime: value.periodTime
                },
                isPause: oldValue.isPause,
                name: value.name,
                tagIds: value.tagIds,
                type: value.type
            });
        else  {
            await useCreateScheduleTransaction({
                accountId: value.accountId,
                amount: value.amount,
                categoryId: value.categoryId,
                isFreeze: value.isFreeze,
                schedule:{
                    dateStart:  value.dateStart.toString(),
                    dateEnd: value.dateEnd?.toString(),
                    period: value.period,
                    periodTime: value.periodTime
                },
                description: value.name,
                name: value.name,
                tagIds: value.tagIds,
                type: value.type
            });
        }

        refreshScheduleTransactions()
            
    } catch(err) {
        toast.add({
            title: 'Error submit transaction',
            description: 'Error while submit transaction ' + err,
            color: 'error'
        })
    }
}

async function openTransaction(id?: string) {
    let scheduleTransaction:ScheduleTransactionType|undefined;
    if (id)
        scheduleTransaction = await fetchScheduleTransaction(id);

    modalScheduleTransaction.open({
        scheduleTransaction: scheduleTransaction,
        onSubmit: onSubmitTransaction 
    }); 
};

const onDelete = async (id: string) => {
    await useDeleteScheduleTransaction(id)
    refreshScheduleTransactions()
}

</script>

<template>
    <div>
        <div style="margin-top: 1rem;">
            <div class="flex justify-end">
                <UButton
                    label="Ajouter transaction"
                    @click="openTransaction()"
                 />
            </div>
            <UTable 
                :data="displayScheluletransactionsTable" 
                :columns="tableColumn" 
                class="flex-1">
                <template #expanded="{ row }">
                    <div class="flex flex-row flex-wrap">
                        <div v-for="tag in row.original.tags" :key="tag.id">
                            <UBadge 
                                :label="tag.value" 
                                :style="{color:tag.color, borderColor: tag.color}" 
                                variant="outline" color="neutral"/>  
                        </div>
                    </div>
                </template>
            </UTable>
            <!-- <div class="flex flex-row gap-2 items-baseline-last justify-between">
                <UPagination 
                    class="mt-3" 
                    v-model:page="page" 
                    v-on:update:page="() => paramsTransactions.offset = page - 1"
                    :items-per-page="paramsTransactions.limit"  
                    :total="Number(transactions?.totals)" 
                    active-variant="subtle" />
                <UInputNumber 
                    v-model="paramsTransactions.limit" 
                    :min="1" 
                    orientation="vertical" 
                    style="width: 80px;"
                />
            </div> -->
        </div>
    </div>
</template>