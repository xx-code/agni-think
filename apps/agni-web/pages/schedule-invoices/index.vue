<script setup lang="ts">
import { ModalEditScheduleInvoice } from '#components';
import { getLocalTimeZone } from '@internationalized/date';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import { listCategoriesResponseToListCategories } from '~/mappers/category';
import { listScheduleInvoicesResponseToListScheduleInvoices, scheduleInvoiceResponseToScheduleInvoice } from '~/mappers/scheduleTransaction';
import { listTagsResponseToListTags } from '~/mappers/tag';
import type { CreatedRequest, ListResponse, QueryFilterRequest } from '~/types/api';
import type { GetCategoryResponse } from '~/types/api/category';
import type { CreateScheduleInvoiceRequest, GetScheduleInvoiceResponse, UpdateScheduleInvoiceRequest } from '~/types/api/scheduleTransaction';
import type { GetTagResponse } from '~/types/api/tag';
import type { EditScheduleInvoiceType, ScheduleInvoiceType } from '~/types/ui/scheduleTransaction';
import type { GetScheduleInvoiceSummaryResponse } from '~/types/api/analytics';
import { scheduleInvoiceSummaryResponseToScheduleInvoiceSummary } from '~/mappers/analytics';
import useLazyInifinteScroll from '~/composables/ui/useLazyInfiniteScroll';
import { useInfiniteScroll } from '@vueuse/core';
import useConfirmModal from '~/composables/modal/useConfirmModal';

const MAX_ITEMS_TO_DISPLAY=100

const toast = useToast();
const overlay = useOverlay()
const el = useTemplateRef('el')
const { open } = useConfirmModal(overlay)
const modalScheduleInvoice = overlay.create(ModalEditScheduleInvoice);

const { data: utils } = useAsyncData('utils+schedule-invoices', async () => {
    const query = { offset: 0, limit: 0, queryAll: true}
    const [categories, tags, scheduleInvoiceSummary] = await Promise.all([
        ApiLinkBuilder.route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES).query(query).mapper(listCategoriesResponseToListCategories).execute(),
        ApiLinkBuilder.route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS).query(query).mapper(listTagsResponseToListTags).execute(), 
        ApiLinkBuilder.route<GetScheduleInvoiceSummaryResponse>(API_ROUTES.ANALYTICS.SCHEDULE_INVOICE).mapper(scheduleInvoiceSummaryResponseToScheduleInvoiceSummary).execute()
    ])

    return {
        categories,
        tags,
        scheduleInvoiceSummary
    }
})

const { data, query, loadData, loading, hasMore, totalData, reset, removeData, updateData } = useLazyInifinteScroll<QueryFilterRequest, GetScheduleInvoiceResponse, ScheduleInvoiceType>(
    API_ROUTES.SCHEDULE_INVOICES.GET_SCHEDULE_INVOICES,
    API_ROUTES.SCHEDULE_INVOICES.GET_SCHEDULE_INVOICE,
    listScheduleInvoicesResponseToListScheduleInvoices,
    scheduleInvoiceResponseToScheduleInvoice,
    {limit: 8, offset: 0, queryAll: false}    
)


async function togglePauseSchedule(id:string, isPause: boolean) {
    const index = data.value.findIndex(i => i.id === id) 
    if (index >= 0) {
        await ApiLinkBuilder.route(API_ROUTES.SCHEDULE_INVOICES.UPDATE_SCHEDULE_INVOICE).params({id}).body({
            isPause: !isPause,
        }).execute()

        updateData(index, id)
    }
}


async function onSubmitTransaction(value: EditScheduleInvoiceType, oldValue?: ScheduleInvoiceType) {
    try {
        if (oldValue) {
            const index = data.value.findIndex(i => i.id === oldValue.id) 
            if (index >= 0) {
                await ApiLinkBuilder.route(API_ROUTES.SCHEDULE_INVOICES.UPDATE_SCHEDULE_INVOICE).params({id: oldValue.id}).body({
                    accountId: value.accountId,
                    amount: value.amount,
                    categoryId: value.categoryId,
                    schedule:{
                        dueDate:  value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                        repeater: {
                            period: value.repeater?.periodType,
                            interval: value.repeater?.interval
                        }
                    },
                    isPause: oldValue.isPause,
                    name: value.name,
                    tagIds: value.tagIds,
                    type: value.type,
                    endDate: value.endDate?.toDate(getLocalTimeZone()).toISOString(),
                    freezeSchedule: (value.freezeEndDate && value.freezeRepeater) ? {
                        dueDate: value.freezeEndDate.toDate(getLocalTimeZone()).toISOString(),
                        repeater: {
                            period: value.freezeRepeater.periodType,
                            interval: value.freezeRepeater.interval
                        } 
                    } : undefined
                } as UpdateScheduleInvoiceRequest).execute()

                updateData(index, oldValue.id)
            }
        } else  {
            await ApiLinkBuilder.route<CreatedRequest>(API_ROUTES.SCHEDULE_INVOICES.CREATE_SCHEDULE_INVOICE).body({
                accountId: value.accountId,
                amount: value.amount,
                categoryId: value.categoryId,
                isFreeze: value.isFreeze,
                schedule:{
                    dueDate:  value.dueDate.toDate(getLocalTimeZone()).toISOString(),
                    repeater: {
                        period: value.repeater?.periodType,
                        interval: value.repeater?.interval
                    }
                },
                description: value.name,
                name: value.name,
                tagIds: value.tagIds,
                type: value.type,
                endDate: value.endDate?.toDate(getLocalTimeZone()).toISOString(),
                freezeSchedule: (value.freezeEndDate && value.freezeRepeater) ? {
                    dueDate: value.freezeEndDate.toDate(getLocalTimeZone()).toISOString(),
                    repeater: {
                        period: value.freezeRepeater.periodType,
                        interval: value.freezeRepeater.interval
                    }
                } : undefined
            } as CreateScheduleInvoiceRequest).execute();
            reset()
            loadData()
        }
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
        scheduleInvoice = await ApiLinkBuilder.route<GetScheduleInvoiceResponse>(API_ROUTES.SCHEDULE_INVOICES.GET_SCHEDULE_INVOICE).params({id}).mapper(scheduleInvoiceResponseToScheduleInvoice).execute();

    modalScheduleInvoice.open({
        scheduleInvoice: scheduleInvoice,
        onSubmit: onSubmitTransaction 
    }); 
};

const onDelete = async (id: string) => {
    const index = data.value.findIndex(i => i.id === id) 
    if (index >= 0) {
        await ApiLinkBuilder.route(API_ROUTES.SCHEDULE_INVOICES.DELETE_SCHEDULE_INVOICE).params({id}).execute()
        removeData(index)
    }
}

useInfiniteScroll(
    el, 
    () => {
        if (!loading.value && hasMore.value && data.value.length < MAX_ITEMS_TO_DISPLAY ) {
            query.offset = data.value.length
        }
    }, {
        distance: 5,
        canLoadMore() {
            return hasMore.value
        },
    }
)

</script>

<template>
    <UiPage>
        <UiPageHeader 
            title="Transaction Planifiées"
            :button="
                {
                    icon: 'i-lucide-plus',
                    label: 'Ajouter une facture planifiée'
                }
            "
            subtitle="Gérez vos transactions récurrentes et planifiées"
            @click-button="openInvoice()"
        />

        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <UiBannerAccountant 
                title="Total Planifié"
                :amount="utils?.scheduleInvoiceSummary.totalPlan ?? 0"
                :icon="{ name: 'i-lucide-calendar-clock', backgroundColor: 'rgba(59, 130, 246, 0.1)', fontColor: '#3b82f6' }"
            />

            <UiBannerAccountant 
                title="Total montant active planifié"
                :amount="utils?.scheduleInvoiceSummary.totalAmountActive ?? 0"
                :icon="{ name: 'i-lucide-shield-check', backgroundColor: 'rgba(168, 85, 247, 0.1)', fontColor: '#a855f7' }"
            />

            <UiBannerAccountant 
                title="Actives"
                :amount="utils?.scheduleInvoiceSummary.totalActives ?? 0"
                :icon="{ name: 'i-lucide-play-circle', backgroundColor: 'rgba(16, 185, 129, 0.1)', fontColor: '#10b981' }"
            />

            <UiBannerAccountant 
                title="En Pause"
                :amount="utils?.scheduleInvoiceSummary.totalPause ?? 0 "
                :icon="{ name: 'i-lucide-pause-circle', backgroundColor: 'rgb(255, 115, 4, 0.1)', fontColor: '#ff7304' }"
            />
        </div>


        <div class="flex flex-col gap-4" >
            <UiScheduleInvoiceCard 
                v-for="scheduleInvoice in data"
                :categories="utils?.categories.items ?? []"
                :data="scheduleInvoice"
                @update="openInvoice(scheduleInvoice.id)"
                @delete="onDelete(scheduleInvoice.id)"
                @toggle-pause="togglePauseSchedule(scheduleInvoice.id, scheduleInvoice.isPause)"
            />

            <div ref="el">
                <UiListEndIndicator 
                    :current-size="data.length"
                    :total-data="totalData"
                    :has-more="hasMore"
                    :max-to-display="MAX_ITEMS_TO_DISPLAY"
                    :loading="loading"
                    @show-more="() => { query.offset = data.length }"
                />
            </div>
            
        </div>  

        <div  v-if="loading" class="flex justify-center items-center py-12">
            <div class="flex flex-col items-center gap-3">
                <UIcon name="i-lucide-loader-2" class="w-8 h-8 animate-spin text-primary-500" />
                <p class="text-gray-600 dark:text-gray-400">Chargement...</p>
            </div>
        </div>
    </UiPage>
</template>