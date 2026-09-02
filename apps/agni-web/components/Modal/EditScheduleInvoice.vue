<script setup lang="ts">
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date'
import { reactive, shallowRef } from "vue";
import type { FormError, FormSubmitEvent } from '#ui/types';
import type { EditScheduleInvoiceType, ScheduleInvoiceType } from '~/types/ui/scheduleTransaction';
import type { ListResponse } from '~/types/api';
import type { GetCategoryResponse } from '~/types/api/category';
import type { GetTagResponse } from '~/types/api/tag';
import type { GetAccountResponse } from '~/types/api/account';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import { listCategoriesResponseToListCategories } from '~/mappers/category';
import { listTagsResponseToListTags } from '~/mappers/tag';
import { listAccountsToListAccount } from '~/mappers/account';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';

const { scheduleInvoice } = defineProps<{
    scheduleInvoice?: ScheduleInvoiceType
}>();
const emit = defineEmits<{
    (e: 'submit', value: EditScheduleInvoiceType, oldValue?: ScheduleInvoiceType): void
    (e: 'close', close: boolean): void
}>();

const isSubmitting = ref(false)

// Titre dynamique : reflète le mode édition/ajout, comme le fait déjà le bouton de soumission.
const modalTitle = computed(() => scheduleInvoice ? 'Modifier la transaction planifiée' : 'Nouvelle transaction planifiée')

const { data: utils, status } = useAsyncData('utils+edit-invoices', async () => {
    const query = { offset: 0, limit: 0, queryAll: true, isSystem: false }
    const [categories, tags, accounts, transactionTypes, periodTypes] = await Promise.all([
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
        ApiLinkBuilder
            .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
            .query(query)
            .mapper(listAccountsToListAccount)
            .execute(),
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.TRANSACTION_TYPE).execute(),
        ApiLinkBuilder.route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PERIOD_TYPE).execute()
    ])

    return { categories, tags, accounts, transactionTypes, periodTypes }
})

const DEFAULT_REPEATER = { periodType: 'Day', interval: 1 }

const form = reactive<Partial<EditScheduleInvoiceType>>({
    accountId: scheduleInvoice?.accountId || '',
    categoryId: scheduleInvoice?.categoryId || '',
    name: scheduleInvoice?.name || '',
    tagIds: scheduleInvoice?.tagIds || [],
    amount: scheduleInvoice?.amount || 0,
    repeater: scheduleInvoice?.repeater,
    freezeRepeater: scheduleInvoice?.freezeRepeater,
    type: scheduleInvoice?.type,
    isFreeze: scheduleInvoice?.isFreeze ?? false
})

const isRecurrence = ref(scheduleInvoice?.repeater !== undefined)
function onChangeIsRecurrence(value: boolean) {
    form.repeater = value ? (scheduleInvoice?.repeater ?? { ...DEFAULT_REPEATER }) : undefined
    if (!value) haveToStop.value = false
}

function onChangeIsFreeze(value: boolean) {
    form.freezeRepeater = value ? (scheduleInvoice?.freezeRepeater ?? { ...DEFAULT_REPEATER }) : undefined
    if (value) {
        // Champs sans rapport avec une transaction gelée — on les vide plutôt que de les
        // laisser désactivés-mais-remplis, pour éviter d'envoyer des valeurs fantômes au submit.
        form.categoryId = ''
        form.type = undefined
        form.tagIds = []
    }
}

function toCalendarDate(date: Date) {
    return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

const dueDate = shallowRef(toCalendarDate(scheduleInvoice?.dueDate ?? new Date()))

const haveToStop = ref(!!scheduleInvoice?.endDate)
const endDate = shallowRef(toCalendarDate(scheduleInvoice?.endDate ?? new Date()))

const freezeEndDate = shallowRef(toCalendarDate(scheduleInvoice?.freezeEndDate ?? new Date()))

const df = new DateFormatter('fr-CA', { dateStyle: 'medium' });

function validate(state: Partial<EditScheduleInvoiceType>): FormError[] {
    const errors: FormError[] = []

    if (!state.name) errors.push({ name: 'name', message: 'Requis' })
    if (!state.accountId) errors.push({ name: 'accountId', message: 'Requis' })
    if (!state.isFreeze && !state.categoryId) errors.push({ name: 'categoryId', message: 'Requis' })
    if (!state.amount) errors.push({ name: 'amount', message: 'Requis' })
    if (!dueDate.value) errors.push({ name: 'dueDate', message: 'Requis' })
    if (!state.isFreeze && !state.type) errors.push({ name: 'type', message: 'Requis' })

    if (state.repeater && !state.repeater.periodType) errors.push({ name: 'period', message: 'Requis' })
    if (state.repeater && !state.repeater.interval) errors.push({ name: 'interval', message: 'Requis' })

    if (state.isFreeze && !freezeEndDate.value) errors.push({ name: 'freezeEndDate', message: 'Requis' })
    if (state.isFreeze && !state.freezeRepeater?.periodType) errors.push({ name: 'freezePeriod', message: 'Requis' })
    if (state.isFreeze && !state.freezeRepeater?.interval) errors.push({ name: 'freezeInterval', message: 'Requis' })

    return errors
}

function resetForm() {
    form.accountId = ''
    form.categoryId = ''
    form.name = ''
    form.tagIds = []
    form.amount = 0
    form.repeater = undefined
    form.freezeRepeater = undefined
    form.type = undefined
    form.isFreeze = false

    isRecurrence.value = false
    haveToStop.value = false
    dueDate.value = toCalendarDate(new Date())
    endDate.value = toCalendarDate(new Date())
    freezeEndDate.value = toCalendarDate(new Date())
}

async function onSubmit(event: FormSubmitEvent<EditScheduleInvoiceType>) {
    const data = event.data;
    isSubmitting.value = true
    try {
        emit('submit', {
            accountId: data.accountId!,
            amount: data.amount!,
            categoryId: !data.isFreeze ? data.categoryId! : undefined,
            name: data.name!,
            tagIds: data.tagIds!,
            type: !data.isFreeze ? data.type! : undefined,
            isFreeze: data.isFreeze,
            dueDate: dueDate.value,
            repeater: form.repeater,
            freezeRepeater: form.freezeRepeater,
            freezeEndDate: freezeEndDate.value,
            endDate: haveToStop.value ? endDate.value : undefined
        }, scheduleInvoice)

        // Note : si le parent démonte/remonte cette modale à chaque ouverture (v-if + :key),
        // ce reset est redondant — un nouveau montage repart déjà de zéro. Il ne sert que si
        // l'instance reste vivante entre deux ouvertures.
        resetForm()
        emit('close', true);
    } finally {
        isSubmitting.value = false
    }
};
</script>

<template>
    <UModal :title="modalTitle">
        <template #body>
            <UForm :validate="validate" :state="form" class="space-y-6" @submit="onSubmit">

                <!-- Section : type -->
                <div class="space-y-4">
                    <UFormField label="Transaction gelée" name="isFreeze" description="Une transaction gelée suspend le calcul du budget pendant une période, sans catégorie ni type associé.">
                        <USwitch v-model="form.isFreeze" @update:model-value="onChangeIsFreeze" />
                    </UFormField>

                    <UFormField v-if="!form.isFreeze" label="Type de transaction" name="type">
                        <USelect
                            v-model="form.type"
                            value-key="value"
                            :loading="status === 'pending'"
                            :items="utils?.transactionTypes.map(i => ({ label: i.value, value: i.id }))"
                            class="w-full" />
                    </UFormField>
                </div>

                <USeparator />

                <!-- Section : détails -->
                <div class="space-y-4">
                    <UFormField label="Compte" name="accountId">
                        <USelect
                            v-model="form.accountId"
                            value-key="value"
                            :loading="status === 'pending'"
                            :items="utils?.accounts.items.map(i => ({ value: i.id, label: i.title }))"
                            class="w-full" />
                    </UFormField>

                    <UFormField label="Montant" name="amount">
                        <UInput 
                            v-model="form.amount" 
                            type="number" 
                            class="w-full" />
                    </UFormField>

                    <UFormField label="Description" name="name">
                        <UInput v-model="form.name" class="w-full" />
                    </UFormField>

                    <UFormField v-if="!form.isFreeze" label="Catégorie" name="categoryId">
                        <USelectMenu
                            v-model="form.categoryId"
                            value-key="value"
                            :loading="status === 'pending'"
                            :items="utils?.categories.items.map(i => ({ value: i.id, label: i.title }))"
                            class="w-full" />
                    </UFormField>

                    <UFormField v-if="!form.isFreeze" label="Tags" name="tagIds">
                        <UInputMenu
                            v-model="form.tagIds"
                            multiple
                            value-key="value"
                            :loading="status === 'pending'"
                            :items="utils?.tags.items.map(i => ({ value: i.id, label: i.value }))"
                            class="w-full" />
                    </UFormField>

                    <UFormField label="Date d'échéance" name="dueDate">
                        <UPopover>
                            <UButton color="neutral" variant="subtle" icon="i-lucide-calendar">
                                {{ dueDate ? df.format(dueDate.toDate(getLocalTimeZone())) : 'Sélectionnez une date' }}
                            </UButton>
                            <template #content>
                                <UCalendar v-model="dueDate" />
                            </template>
                        </UPopover>
                    </UFormField>
                </div>

                <!-- Section : gel -->
                <template v-if="form.isFreeze">
                    <USeparator />
                    <div class="space-y-4">
                        <p class="text-sm font-medium text-highlighted">Paramètres du gel</p>

                        <UFormField label="Date de fin du gel" name="freezeEndDate">
                            <UPopover>
                                <UButton color="neutral" variant="subtle" icon="i-lucide-calendar">
                                    {{ freezeEndDate ? df.format(freezeEndDate.toDate(getLocalTimeZone())) : 'Sélectionnez une date' }}
                                </UButton>
                                <template #content>
                                    <UCalendar v-model="freezeEndDate" />
                                </template>
                            </UPopover>
                        </UFormField>

                        <UFormField label="Période de gel" name="freezePeriod">
                            <USelect
                                v-model="form.freezeRepeater!.periodType"
                                value-key="value"
                                :items="utils?.periodTypes.map(i => ({ label: i.value, value: i.id }))"
                                class="w-full" />
                        </UFormField>

                        <UFormField label="Intervalle de gel" name="freezeInterval">
                            <UInput v-model="form.freezeRepeater!.interval" type="number" :min="1" class="w-full" />
                        </UFormField>
                    </div>
                </template>

                <USeparator />

                <!-- Section : récurrence -->
                <div class="space-y-4">
                    <UFormField label="Se répète" name="isRecurrence">
                        <USwitch v-model="isRecurrence" @update:model-value="onChangeIsRecurrence" />
                    </UFormField>

                    <template v-if="form.repeater">
                        <UFormField label="Période" name="period">
                            <USelect
                                v-model="form.repeater.periodType"
                                value-key="value"
                                :items="utils?.periodTypes.map(i => ({ label: i.value, value: i.id }))"
                                class="w-full" />
                        </UFormField>

                        <UFormField label="Intervalle" name="interval">
                            <UInput v-model="form.repeater.interval" type="number" :min="1" class="w-full" />
                        </UFormField>

                        <UFormField label="Se termine à une date précise" name="haveEndDate">
                            <USwitch v-model="haveToStop" />
                        </UFormField>

                        <UFormField v-if="haveToStop" label="Date d'arrêt" name="endDate">
                            <UPopover>
                                <UButton color="neutral" variant="subtle" icon="i-lucide-calendar">
                                    {{ endDate ? df.format(endDate.toDate(getLocalTimeZone())) : 'Sélectionnez une date' }}
                                </UButton>
                                <template #content>
                                    <UCalendar v-model="endDate" />
                                </template>
                            </UPopover>
                        </UFormField>
                    </template>
                </div>

                <!-- Footer -->
                <div class="flex justify-end gap-2 pt-2 border-t border-default">
                    <UButton
                        label="Annuler"
                        variant="outline"
                        color="neutral"
                        :disabled="isSubmitting"
                        @click="emit('close', false)" />
                    <UButton
                        type="submit"
                        :label="scheduleInvoice ? 'Mettre à jour' : 'Ajouter'"
                        :loading="isSubmitting"
                        :disabled="isSubmitting" />
                </div>
            </UForm>
        </template>
    </UModal>
</template>