<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { NuxtError } from "#app";
import type { ErrorResponse, ListResponse } from '~/types/api';
import type { CreatedRequest } from '~/types/api';
import type { GetAccountResponse } from '~/types/api/account';
import type { GetCategoryResponse } from '~/types/api/category';
import type { GetTagResponse } from '~/types/api/tag';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import { listAccountsToListAccount } from '~/mappers/account';
import { listCategoriesResponseToListCategories } from '~/mappers/category';
import { listTagsResponseToListTags } from '~/mappers/tag';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import { ProvisionType, DEPRECIATE_TYPE_CONFIG, DepreciateType } from '~/types/constants/provision';
import type { EditProvision, Provision } from '~/types/ui/provision';
import type { CreateProvisionRequest, UpdateProvisionRequest } from "~/types/api/provision";


const { provision } = defineProps<{
    provision?: Provision
}>();

const emit = defineEmits<{
    (e: 'close', doRefresh: boolean): void
}>();

const toast = useToast()
const isLoading = ref(false)


const isUpdate = !!provision

const { data: utils } = useAsyncData('provision-utils', async () => {
    isLoading.value = true
    const query = { offset: 0, limit: 0, queryAll: true, isSystem: false }
    const [accounts, categories, tags, periodTypes] = await Promise.all([
        ApiLinkBuilder
            .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
            .query(query)
            .mapper(listAccountsToListAccount)
            .execute(),
        ApiLinkBuilder
            .route<ListResponse<GetCategoryResponse>>(API_ROUTES.CATEGORIES.GET_CATEGORIES)
            .query(query)
            .mapper(listCategoriesResponseToListCategories)
            .execute(),
        ApiLinkBuilder
            .route<ListResponse<GetTagResponse>>(API_ROUTES.TAGS.GET_TAGS)
            .query(query)
            .mapper(listTagsResponseToListTags)
            .execute(),
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PERIOD_TYPE).execute()
    ])
    isLoading.value = false
    return {
        accounts: accounts.items,
        categories: categories.items,
        tags: tags.items,
        periodTypes: periodTypes
    }
})

const form = reactive<Partial<EditProvision>>({
    title: provision?.title || '',
    costHT: provision?.costHT || 0,
    costTTC: provision?.costTTC || 0,
    expectedLifespanMonth: provision?.expectedLifespanMonth || 0,
    isPatrimony: provision?.isPatrimony ?? false,
    floorValue: provision?.floorValue || 0,
    interestLoan: provision?.interestLoan || 0,
    loanMonth: provision?.loanMonth || 0,
    type: provision?.type || ProvisionType.Depreciate,
    depreciationCriteria: (provision?.depreciationCriteria as any[] || []).map((c: any) => ({
        title: c.title,
        description: c.description,
        type: c.type as DepreciateType,
        value: c.value,
        monthRange: c.monthRange
    })),
    scheduleInvoice: provision?.scheduleInvoice ? {
        accountId: provision.scheduleInvoice.accountId || provision.scheduleInvoice.accountId || '',
        categoryId: provision.scheduleInvoice.categoryId || provision.scheduleInvoice.categoryId || '',
        tagIds: provision.scheduleInvoice.tagIds || [],
        budgetIds: provision.scheduleInvoice.budgetIds || [],
        paymentPeriod: provision.scheduleInvoice.paymentPeriod,
        paymentInterval: provision.scheduleInvoice.paymentInterval
    } : undefined
})

const isDepreciateLoan = computed(() => form.type === ProvisionType.DepreciateLoan)

watch(isDepreciateLoan, (val) => {
    if (val && !form.scheduleInvoice) initScheduleInvoice()
}, { immediate: true })

function toCalendarDate(date: Date): CalendarDate {
    return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

const rawAcquisitionDate = provision?.acquisitionDate
    ? (provision.acquisitionDate instanceof Date ? provision.acquisitionDate : new Date(provision.acquisitionDate))
    : new Date()
const acquisitionDate = shallowRef(toCalendarDate(rawAcquisitionDate))


const df = new DateFormatter('en-Us', { dateStyle: 'medium' })

function onChangeHasSchedulerRepeater(val: boolean) {
    // if (val) {
    //     if (!form.scheduleInvoice) initScheduleInvoice()
    //     form.scheduleInvoice!.scheduler.repeater = { period: 'Month', interval: 1 }
    // } else {
    //     if (form.scheduleInvoice) form.scheduleInvoice.scheduler.repeater = undefined
    // }
}

function initScheduleInvoice() {
    form.scheduleInvoice = {
        accountId: '',
        categoryId: '',
        tagIds: [],
        budgetIds: [],
        paymentInterval: 0,
        paymentPeriod: ''
    }
}

function addDepreciationCriteria() {
    if (!form.depreciationCriteria) form.depreciationCriteria = []
    form.depreciationCriteria.push({
        title: '',
        description: '',
        type: 'StraightLine' as DepreciateType,
        value: 0,
        monthRange: 0
    })
}

function removeDepreciationCriteria(index: number) {
    form.depreciationCriteria?.splice(index, 1)
}

function validate(state: Partial<EditProvision>): FormError[] {
    const errors: FormError[] = []

    if (!state.title) errors.push({ name: 'title', message: 'Required' })
    if (!state.costHT || state.costHT <= 0) errors.push({ name: 'costHT', message: 'Le cout HT doit etre superieur a zero' })
    if (!state.costTTC || state.costTTC <= 0) errors.push({ name: 'costTTC', message: 'Le cout TTC doit etre superieur a zero' })
    if (!state.expectedLifespanMonth || state.expectedLifespanMonth <= 0) errors.push({ name: 'expectedLifespanMonth', message: 'Le temps de vie doit etre superieur a zero' })
    if (!acquisitionDate.value) errors.push({ name: 'acquisitionDate', message: 'Required' })
    if (!state.type) errors.push({ name: 'type', message: 'Required' })

    if (state.type === ProvisionType.DepreciateLoan) {
        if (state.interestLoan === undefined || state.interestLoan < 0) errors.push({ name: 'interestLoan', message: 'Le taux d\'interet doit etre positif' })
        if (!state.loanMonth || state.loanMonth <= 0) errors.push({ name: 'loanMonth', message: 'Le nombre de mois de pret doit etre superieur a zero' })

        if (state.scheduleInvoice) {
            if (!state.scheduleInvoice.accountId) errors.push({ name: 'accountId', message: 'Le compte est requis' })
            if (!state.scheduleInvoice.categoryId) errors.push({ name: 'categoryId', message: 'La categorie est requise' })
            if (!state.scheduleInvoice.paymentPeriod) errors.push({ name: 'period', message: 'La date periode de payment est requise' })
            if (!state.scheduleInvoice.paymentInterval) errors.push({ name: 'interval', message: "L'interval est requise" })
        }
    }

    console.log(errors)

    return errors
}

async function onSubmit(event: FormSubmitEvent<EditProvision>) {
    isLoading.value = true
    let isSuccess = false
    let resError: ErrorResponse | undefined = undefined

    const data = event.data
    const acquisitionDateStr = acquisitionDate.value.toDate(getLocalTimeZone()).toISOString()
    console.log(data)

    if (isUpdate) {
        const body: UpdateProvisionRequest = {
            title: data.title,
            costHT: data.costHT,
            costTTC: data.costTTC,
            acquisitionDate: acquisitionDateStr,
            expectedLifespanMonth: data.expectedLifespanMonth,
            type: data.type,
            isPatrimony: data.isPatrimony,
            floorValue: data.floorValue,
            interestLoan: data.interestLoan,
            loanMonth: data.loanMonth,
            depreciationCriteria: (data.depreciationCriteria || []).map(c => ({
                title: c.title,
                description: c.description,
                type: c.type,
                value: c.value,
                monthRange: c.monthRange
            }))
        }

        if (data.type === ProvisionType.DepreciateLoan && data.scheduleInvoice) {
            const doUpdateLoan = data.costTTC !== provision?.costTTC || data.loanMonth !== provision?.loanMonth
            if (doUpdateLoan || !provision?.scheduleInvoice) {
                body.scheduleInvoice = {
                    invoiceAccountId: data.scheduleInvoice.accountId,
                    invoiceCategoryId: data.scheduleInvoice.categoryId,
                    tagIds: data.scheduleInvoice.tagIds,
                    budgetIds: data.scheduleInvoice.budgetIds,
                    paymentPeriod: data.scheduleInvoice.paymentPeriod,
                    paymentInterval: data.scheduleInvoice.paymentInterval
                }
            }
        }

        try {
            await ApiLinkBuilder
                .route(API_ROUTES.PROVISIONS.UPDATE_PROVISION)
                .params({ id: provision.id })
                .body(body)
                .execute()
            isSuccess = true
        } catch (err) {
            resError = (err as NuxtError).data as ErrorResponse
        }
    } else {
        const body: CreateProvisionRequest = {
            title: data.title,
            costHT: data.costHT,
            costTTC: data.costTTC,
            acquisitionDate: acquisitionDateStr,
            expectedLifespanMonth: data.expectedLifespanMonth,
            type: data.type,
            isPatrimony: data.isPatrimony,
            floorValue: data.floorValue,
            interestLoan: data.interestLoan,
            loanMonth: data.loanMonth,
            depreciationCriteria: (data.depreciationCriteria || []).map(c => ({
                title: c.title,
                description: c.description,
                type: c.type,
                value: c.value,
                monthRange: c.monthRange
            })),
        }

        if (data.type === ProvisionType.DepreciateLoan && data.scheduleInvoice) {
            body.scheduleInvoice = {
                invoiceAccountId: data.scheduleInvoice.accountId,
                invoiceCategoryId: data.scheduleInvoice.categoryId,
                tagIds: data.scheduleInvoice.tagIds,
                budgetIds: data.scheduleInvoice.budgetIds,
                paymentInterval: data.scheduleInvoice.paymentInterval,
                paymentPeriod: data.scheduleInvoice.paymentPeriod
            }
        }

        try {
            await ApiLinkBuilder
                .route<CreatedRequest>(API_ROUTES.PROVISIONS.CREATE_PROVISION)
                .body(body)
                .execute()
            isSuccess = true
        } catch (err) {
            console.log(err)
            resError = (err as NuxtError).data as ErrorResponse
        }
    }

    isLoading.value = false

    if (isSuccess) {
        toast.add({
            title: isUpdate ? 'Provision mise a jour' : 'Provision creee',
            color: 'success'
        })
        emit('close', true)
    } else {
        toast.add({
            title: 'Erreur',
            description: resError?.message || 'Une erreur est survenue',
            color: 'error'
        })
    }
}
</script>

<template>
    <UModal :title="provision ? 'Modifier la provision' : 'Nouvelle provision'" :ui="{ body: 'max-h-[80vh] overflow-y-auto' }">
        <template #body>
            <div v-if="isLoading && !utils" class="flex justify-center py-8">
                <LoadingIndicator />
            </div>
            <UForm v-else :state="form" :validate="validate" @submit="onSubmit" class="space-y-6">
                <UFormField label="Titre" name="title">
                    <UInput v-model="form.title" />
                </UFormField>

                <UFormField label="Type de provision" name="type">
                    <USelect
                        v-model="form.type"
                        value-key="value"
                        :items="[
                            { label: 'Actif Deprecie', value: ProvisionType.Depreciate },
                            { label: 'Actif Deprecie avec pret', value: ProvisionType.DepreciateLoan }
                        ]"
                    />
                </UFormField>

                <UFormField label="Cout HT" name="costHT">
                    <UInput v-model="form.costHT" type="number" />
                </UFormField>

                <UFormField label="Cout TTC" name="costTTC">
                    <UInput v-model="form.costTTC" type="number" />
                </UFormField>

                <UFormField label="Temps de vie en mois" name="expectedLifespanMonth">
                    <UInput v-model="form.expectedLifespanMonth" type="number" />
                </UFormField>

                <UFormField label="Valeur planche" name="floorValue">
                    <UInput v-model="form.floorValue" type="number" />
                </UFormField>

                <UFormField label="Est un patrimoine" name="isPatrimony">
                    <USwitch v-model="form.isPatrimony" />
                </UFormField>

                <UFormField label="Date d'acquisition" name="acquisitionDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar">
                            {{ acquisitionDate ? df.format(acquisitionDate.toDate(getLocalTimeZone())) : 'Selectionnez une date' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="acquisitionDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <template v-if="isDepreciateLoan">
                    <UDivider label="Parametres du pret" />

                    <UFormField label="Taux d'interet (%)" name="interestLoan">
                        <UInput v-model="form.interestLoan" type="number" />
                    </UFormField>

                    <UFormField label="Nombre de mois de pret" name="loanMonth">
                        <UInput v-model="form.loanMonth" type="number" />
                    </UFormField>

                    <UDivider label="Facturation du pret" />

                    <UFormField label="Compte" name="accountId">
                        <USelect
                            v-model="form.scheduleInvoice!.accountId"
                            value-key="value"
                            :items="utils?.accounts.map(i => ({ value: i.id, label: i.title }))"
                        />
                    </UFormField>

                    <UFormField label="Categorie" name="categoryId">
                        <USelectMenu
                            v-model="form.scheduleInvoice!.categoryId"
                            value-key="value"
                            :items="utils?.categories.map(i => ({ value: i.id, label: i.title }))"
                        />
                    </UFormField>

                    <UFormField label="Tags" name="tagIds">
                        <UInputMenu
                            v-model="form.scheduleInvoice!.tagIds"
                            multiple
                            value-key="value"
                            :items="utils?.tags.map(i => ({ value: i.id, label: i.value }))"
                        />
                    </UFormField>

                    <!-- <UFormField label="Est repetitif" name="hasSchedulerRepeater">
                        <USwitch v-model="hasSchedulerRepeater" @update:model-value="onChangeHasSchedulerRepeater" />
                    </UFormField> -->

                    <UFormField v-if="form.scheduleInvoice" label="Periode" name="period">
                        <USelect 
                            v-model="form.scheduleInvoice!.paymentPeriod"
                            value-key="value"
                            :items="utils?.periodTypes.map(i => ({ label: i.value, value: i.id }))"
                        />
                    </UFormField>

                    <UFormField v-if="form.scheduleInvoice" label="Intervalle" name="interval">
                        <UInput v-model="form.scheduleInvoice!.paymentInterval" type="number" />
                    </UFormField>
                </template>

                <UDivider label="Criteres d'amortissement" />

                <div v-for="(criteria, index) in form.depreciationCriteria" :key="index" class="space-y-3 p-4 border rounded-lg">
                    <div class="flex justify-between items-center">
                        <span class="text-sm font-medium">Critere {{ index + 1 }}</span>
                        <UButton
                            icon="i-lucide-trash-2"
                            color="error"
                            variant="ghost"
                            size="xs"
                            @click="removeDepreciationCriteria(index)"
                        />
                    </div>

                    <UFormField label="Titre" :name="`criteria-title-${index}`">
                        <UInput v-model="criteria.title" />
                    </UFormField>

                    <UFormField label="Description" :name="`criteria-desc-${index}`">
                        <UInput v-model="criteria.description" />
                    </UFormField>

                    <UFormField label="Type" :name="`criteria-type-${index}`">
                        <USelect
                            v-model="criteria.type"
                            value-key="value"
                            :items="Object.entries(DEPRECIATE_TYPE_CONFIG).map(([key, label]) => ({ label, value: key }))"
                        />
                    </UFormField>

                    <div class="grid grid-cols-2 gap-4">
                        <UFormField label="Valeur" :name="`criteria-value-${index}`">
                            <UInput v-model="criteria.value" type="number" />
                        </UFormField>

                        <UFormField v-if="criteria.type === DepreciateType.DecliningBalance" label="Mois" :name="`criteria-monthRange-${index}`">
                            <UInput v-model="criteria.monthRange" type="number" />
                        </UFormField>
                    </div>
                </div>

                <UButton
                    label="Ajouter un critere"
                    icon="i-lucide-plus"
                    variant="outline"
                    @click="addDepreciationCriteria"
                />

                <UFormField>
                    <UButton :label="provision ? 'Mettre a jour' : 'Creer'" type="submit" :loading="isLoading" />
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>
