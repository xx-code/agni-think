<script setup lang="ts">
import { ModalEditScheduleInvoice } from '#components';
import type { TableColumn, TableRow } from '#ui/types';
import { getLocalTimeZone } from '@internationalized/date';
import useCategories from '~/composables/categories/useCategories';
import useCreateScheduleInvoice from '~/composables/scheduleTransactions/useCreateScheduleTransaction';
import useDeleteScheduleInvoice from '~/composables/scheduleTransactions/useDeleteScheduleTransaction';
import { fetchScheduleInvoice } from '~/composables/scheduleTransactions/useScheduleTransaction';
import useScheduleInvoice from '~/composables/scheduleTransactions/useScheduleTransactions';
import useUpdateScheduleInvoice from '~/composables/scheduleTransactions/useUpdateScheduleTransaction';
import useTags from '~/composables/tags/useTags';
import type { QueryFilterRequest } from '~/types/api';
import type { EditScheduleInvoiceType, ScheduleInvoiceType, TableScheduleInvoiceType } from '~/types/ui/scheduleTransaction';

const {data: categories, error: errorCategory, refresh: refreshCategory } = useCategories({
    queryAll: true, offset: 0, limit: 0
});
const {data: tags, error: errorTag, refresh: refreshTag } = useTags({
    queryAll: true, offset: 0, limit: 0
});
const toast = useToast();

const getCategory = (id: string) => categories.value?.items.find(i => id === i.id)
const getTag = (id: string) => tags.value?.items.find(i => id === i.id)
const page = ref(1)
const scheduleFilter = reactive<QueryFilterRequest>({
    limit: 8,
    offset: 0,
    queryAll: false
})
const {
    data: scheduleInvoices, 
    error: errorTransactions, 
    refresh: refreshScheduleInvoices } = useScheduleInvoice(scheduleFilter);
const displayScheluletransactionsTable = computed(() => {
    return scheduleInvoices.value?.items.map(i => ({
        id: i.id,
        amount: i.amount,
        name: i.name,
        type: i.type,
        isPause: i.isPause,
        isFreeze: i.isFreeze,
        dueDate: i.dueDate, 
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
    } satisfies TableScheduleInvoiceType))    
})

async function togglePauseSchedule(id:string, isPause: boolean) {
    await useUpdateScheduleInvoice(id, {
        isPause: !isPause,
    });

    refreshScheduleInvoices();
}

const UIcon = resolveComponent('UIcon');
const UButton = resolveComponent('UButton');
const UDropdownMenu = resolveComponent('UDropdownMenu');
const UBadge = resolveComponent('UBadge');
const tableColumn: TableColumn<TableScheduleInvoiceType>[] = [
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
                        size: 'sm',
                        'aria-label': 'Expand',
                        ui: {
                            leadingIcon: [
                                'transition-transform duration-300 ease-in-out',
                                row.getIsExpanded() ? 'rotate-180': ''
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
                    class: 'flex items-center justify-center rounded-xl shadow-sm transition-transform hover:scale-105',
                    style: {
                        background: `linear-gradient(135deg, ${color}22, ${color}11)`,
                        width: '42px',
                        height: '42px',
                        border: `1.5px solid ${color}33`
                    }
                }, [
                    h(UIcon, { 
                        name: icon, 
                        class: 'text-xl',
                        style:{color: color}  
                    })
                ])
            )
        }
    },
    {
        accessorKey: 'dueDate',
        header: () => h('div', { class: 'font-semibold text-gray-700' }, 'Date d\'échéance'),
        cell: ({ row }) => {
            return h('div', { class: 'text-sm text-gray-600 font-medium' }, formatDate(row.getValue('dueDate')))
        }
    },
    {
        accessorKey: 'category.title',
        header: () => h('div', { class: 'font-semibold text-gray-700' }, 'Catégorie'),
        cell: ({ row }) => {
            return h('div', { class: 'text-sm font-medium text-gray-800' }, row.original.category.title)
        }
    },
    {
        accessorKey: 'name',
        header: () => h('div', { class: 'font-semibold text-gray-700' }, 'Nom'),
        cell: ({ row }) => {
            return h('div', { class: 'text-sm font-medium text-gray-800' }, row.getValue('name'))
        }
    }, 
    {
        accessorKey: 'isPause',
        header: () => h('div', { class: 'text-center font-semibold text-gray-700' }, 'Statut'),
        cell: ({ row }) => {
            if (!row.original.isPause)
                return h('div', { class: 'flex justify-center' }, 
                    h(UButton, { 
                        icon: 'i-lucide-pause', 
                        size: 'sm', 
                        class: 'rounded-full shadow-sm hover:shadow-md transition-all',
                        variant: 'soft',
                        color: 'success',
                        'aria-label': 'Mettre en pause',
                        onClick: () => togglePauseSchedule(row.original.id, row.original.isPause)
                    })
                )

            return h('div', { class: 'flex justify-center' },
                h(UButton, { 
                    icon: 'i-lucide-play', 
                    size: 'sm', 
                    class: 'rounded-full shadow-sm hover:shadow-md transition-all',
                    variant: 'soft',
                    color: 'error',
                    'aria-label': 'Reprendre',
                    onClick: () => togglePauseSchedule(row.original.id, row.original.isPause)
                })
            )
        }
    },
    {
        accessorKey: 'amount',
        header: () => h('div', { class: 'text-right font-semibold text-gray-700' }, 'Montant'),
        cell: ({ row }) => {
            const amount = Number.parseFloat(row.getValue('amount'))
            const type = row.original.type
            
            const formatted = new Intl.NumberFormat('en-US', {
                style: 'currency',
                currency: 'CAD'
            }).format(amount)

            return h('div', {
                class: 'text-right font-bold text-base',
                style: {color: type === 'Income' ? "#10b981" : "#ef4444"}
            }, formatted)
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
                        'arial-label': 'Actions'
                    },
                    () => 
                        h(
                            UButton, {
                                icon: 'i-lucide-ellipsis-vertical',
                                color: 'neutral',
                                variant: 'ghost',
                                size: 'sm',
                                class: 'ml-auto hover:bg-gray-100',
                                'aria-label': 'Menu actions'
                            }
                        )
                )
            )
        }
    }
]

function getRowItems(rows: TableRow<TableScheduleInvoiceType>) {
    return [
        {
            label: 'Modifier',
            icon: 'i-lucide-pencil',
            disabled: rows.original.isFreeze,
            onSelect: () => {
                const id = rows.original.id
                openInvoice(id)
            }
        },
        {
            label: 'Supprimer',
            icon: 'i-lucide-trash-2',
            onSelect: () => {
                const id = rows.original.id
                if (confirm("Voulez-vous vraiment supprimer cette transaction ?"))
                    onDelete(id)
            }
        }
    ] 
}

const overlay = useOverlay()
const modalScheduleInvoice = overlay.create(ModalEditScheduleInvoice);

async function onSubmitTransaction(value: EditScheduleInvoiceType, oldValue?: ScheduleInvoiceType) {
    try {
        if (oldValue)
            await useUpdateScheduleInvoice(oldValue.id, {
                accountId: value.accountId,
                amount: value.amount,
                categoryId: value.categoryId,
                schedule:{
                    dueDate:  value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                    repeater: value.repeater
                },
                isPause: oldValue.isPause,
                name: value.name,
                tagIds: value.tagIds,
                type: value.type
            });
        else  {
            await useCreateScheduleInvoice({
                accountId: value.accountId,
                amount: value.amount,
                categoryId: value.categoryId,
                isFreeze: value.isFreeze,
                schedule:{
                    dueDate:  value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                    repeater: value.repeater
                },
                description: value.name,
                name: value.name,
                tagIds: value.tagIds,
                type: value.type
            });
        }

        refreshScheduleInvoices()
            
    } catch(err) {
        toast.add({
            title: 'Erreur lors de la soumission',
            description: 'Une erreur est survenue: ' + err,
            color: 'error'
        })
    }
}

async function openInvoice(id?: string) {
    let scheduleInvoice:ScheduleInvoiceType|undefined;
    if (id)
        scheduleInvoice = await fetchScheduleInvoice(id);

    modalScheduleInvoice.open({
        scheduleInvoice: scheduleInvoice,
        onSubmit: onSubmitTransaction 
    }); 
};

const onDelete = async (id: string) => {
    await useDeleteScheduleInvoice(id)
    refreshScheduleInvoices()
}

</script>

<template>
    <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-4 md:p-6 lg:p-8">
        <div class="max-w-7xl mx-auto">
            <!-- Header Section -->
            <div class="mb-8">
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <h1 class="text-3xl font-bold text-gray-900 mb-2">
                            Transactions Planifiées
                        </h1>
                        <p class="text-gray-600 text-sm">
                            Gérez vos transactions récurrentes et planifiées
                        </p>
                    </div>
                    <UButton
                        label="Nouvelle Transaction"
                        icon="i-lucide-plus"
                        size="lg"
                        color="primary"
                        class="shadow-lg hover:shadow-xl transition-shadow"
                        @click="openInvoice()"
                    />
                </div>
            </div>

            <!-- Stats Cards (Optional Enhancement) -->
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="p-3 bg-blue-100 rounded-lg">
                            <UIcon name="i-lucide-calendar-clock" class="text-blue-600 text-xl" />
                        </div>
                        <div>
                            <p class="text-sm text-gray-600">Total Planifié</p>
                            <p class="text-2xl font-bold text-gray-900">{{ scheduleInvoices?.total || 0 }}</p>
                        </div>
                    </div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="p-3 bg-green-100 rounded-lg">
                            <UIcon name="i-lucide-play-circle" class="text-green-600 text-xl" />
                        </div>
                        <div>
                            <p class="text-sm text-gray-600">Actives</p>
                            <p class="text-2xl font-bold text-gray-900">
                                {{ displayScheluletransactionsTable?.filter(t => !t.isPause).length || 0 }}
                            </p>
                        </div>
                    </div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="p-3 bg-orange-100 rounded-lg">
                            <UIcon name="i-lucide-pause-circle" class="text-orange-600 text-xl" />
                        </div>
                        <div>
                            <p class="text-sm text-gray-600">En Pause</p>
                            <p class="text-2xl font-bold text-gray-900">
                                {{ displayScheluletransactionsTable?.filter(t => t.isPause).length || 0 }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Table Card -->
            <div class="bg-white rounded-2xl shadow-lg border border-gray-200 overflow-hidden">
                <div class="overflow-x-auto">
                    <UTable 
                        :data="displayScheluletransactionsTable" 
                        :columns="tableColumn"
                        class="w-full"
                        :ui="{
                            tbody: 'divide-y divide-gray-100',
                            tr: 'hover:bg-gray-50 transition-colors',
                            th: 'bg-gray-50 py-4 px-6',
                            td: 'py-4 px-6'
                        }">
                        <template #expanded="{ row }">
                            <div class="px-6 py-4 bg-gray-50 border-t border-gray-100">
                                <div class="flex items-center gap-2 mb-2">
                                    <UIcon name="i-lucide-tags" class="text-gray-500 text-sm" />
                                    <span class="text-sm font-medium text-gray-700">Étiquettes:</span>
                                </div>
                                <div class="flex flex-wrap gap-2">
                                    <UBadge 
                                        v-for="tag in row.original.tags" 
                                        :key="tag.id"
                                        :label="tag.value" 
                                        :style="{
                                            color: tag.color, 
                                            borderColor: tag.color,
                                            backgroundColor: `${tag.color}15`
                                        }" 
                                        variant="outline" 
                                        color="neutral"
                                        class="px-3 py-1 font-medium"
                                    />
                                </div>
                            </div>
                        </template>
                    </UTable>
                </div>

                <!-- Pagination Footer -->
                <div class="border-t border-gray-200 bg-gray-50 px-6 py-4">
                    <div class="flex flex-col sm:flex-row gap-4 items-center justify-between">
                        <div class="flex items-center gap-3">
                            <span class="text-sm text-gray-600">Lignes par page:</span>
                            <UInputNumber 
                                v-model="scheduleFilter.limit" 
                                :min="1" 
                                :max="50"
                                orientation="vertical" 
                                size="sm"
                                class="w-20"
                            />
                        </div>
                        <UPagination 
                            v-model:page="page" 
                            v-on:update:page="() => scheduleFilter.offset = (scheduleFilter.limit * (page - 1))"
                            :items-per-page="scheduleFilter.limit"  
                            :total="scheduleInvoices?.total" 
                            active-variant="solid"
                            :ui="{
                                list: 'gap-1',
                                item:'rounded-lg'
                            }"
                        />
                    </div>
                </div>
            </div>

            <!-- Empty State -->
            <div 
                v-if="!displayScheluletransactionsTable || displayScheluletransactionsTable.length === 0"
                class="bg-white rounded-2xl shadow-lg border border-gray-200 p-12 text-center mt-6"
            >
                <div class="max-w-md mx-auto">
                    <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <UIcon name="i-lucide-calendar-x" class="text-gray-400 text-3xl" />
                    </div>
                    <h3 class="text-xl font-semibold text-gray-900 mb-2">
                        Aucune transaction planifiée
                    </h3>
                    <p class="text-gray-600 mb-6">
                        Commencez par créer votre première transaction récurrente
                    </p>
                    <UButton
                        label="Créer une transaction"
                        icon="i-lucide-plus"
                        size="lg"
                        @click="openInvoice()"
                    />
                </div>
            </div>
        </div>
    </div>
</template>