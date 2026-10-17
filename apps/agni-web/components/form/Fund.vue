<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { FundForm } from '~/types/form/fund';
import type { Account } from '~/types/ui/account';
import { FUND_TYPE_LIST, FundType, getLabelFundType } from '~/types/constants/fund';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import { ref, watch, shallowRef } from 'vue';

const form = defineModel<Partial<FundForm>>()
const { accounts, validate } = defineProps<{
    accounts: Account[]
    validate: (state: Partial<FundForm>) => FormError[]
}>()
const emit = defineEmits<{
    submit: [event: FormSubmitEvent<FundForm>],
    close: []
}>()

type SinkMode = 'contributions' | 'endDate'

const DEFAULT_REPEATER = { period: 'Day', interval: 1 }
const PERIOD_ITEMS = ['Day', 'Week', 'Month', 'Year']
const SINK_MODE_ITEMS = [
    { label: 'Nombre de contributions', value: 'contributions' },
    { label: 'Date de fin', value: 'endDate' }
]

function toCalendarDate(date: Date) {
    return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

function parseDate(value?: string | Date) {
    if (!value) return new Date()
    return value instanceof Date ? value : new Date(value)
}

const df = new DateFormatter('fr-CA', { dateStyle: 'medium' })
const sinkStartDate = shallowRef(toCalendarDate(parseDate(form.value?.sinkingFundDetail?.startDate)))
const schedulerDate = shallowRef(toCalendarDate(parseDate(form.value?.sinkingFundDetail?.scheduler?.date)))
const sinkMode = ref<SinkMode>(form.value?.sinkingFundDetail?.scheduler ? 'endDate' : 'contributions')

const isCyclic = ref(form.value?.sinkingFundDetail?.scheduler?.repeater !== undefined)

watch(() => form.value?.sinkingFundDetail?.scheduler?.repeater, (repeater) => {
    isCyclic.value = repeater !== undefined
})

function createSinkingFundDetail() {
    return {
        startDate: sinkStartDate.value.toDate(getLocalTimeZone()).toISOString(),
        numberOfContributions: form.value?.sinkingFundDetail?.numberOfContributions || 1,
        contributionRecurrence: form.value?.sinkingFundDetail?.contributionRecurrence ?? { ...DEFAULT_REPEATER },
        scheduler: sinkMode.value === 'endDate'
            ? {
                date: schedulerDate.value.toDate(getLocalTimeZone()).toISOString(),
                repeater: form.value?.sinkingFundDetail?.scheduler?.repeater ?? { ...DEFAULT_REPEATER }
            }
            : undefined
    }
}

function syncSinkingFundDetail() {
    if (!form.value) return
    form.value.sinkingFundDetail = createSinkingFundDetail()
}

watch(() => form.value?.type, (newType) => {
    if (!form.value) return
    if (newType === FundType.SinkingFund) {
        if (!form.value.sinkingFundDetail) syncSinkingFundDetail()
    } else {
        form.value.sinkingFundDetail = undefined
    }
})

watch(sinkMode, (mode) => {
    if (form.value?.type !== FundType.SinkingFund) return
    if (mode === 'contributions' && (!form.value.sinkingFundDetail?.numberOfContributions || form.value.sinkingFundDetail.numberOfContributions < 1)) {
        form.value.sinkingFundDetail = { ...createSinkingFundDetail(), numberOfContributions: 1 }
    } else {
        syncSinkingFundDetail()
    }
})

watch(sinkStartDate, () => {
    if (form.value?.sinkingFundDetail) form.value.sinkingFundDetail.startDate = sinkStartDate.value.toDate(getLocalTimeZone()).toISOString()
})

watch(schedulerDate, () => {
    if (form.value?.sinkingFundDetail?.scheduler) form.value.sinkingFundDetail.scheduler.date = schedulerDate.value.toDate(getLocalTimeZone()).toISOString()
})

if (form.value?.type === FundType.SinkingFund && !form.value.sinkingFundDetail) {
    syncSinkingFundDetail()
}
</script>

<template>
    <UForm v-if="form" :validate="validate" :state="form" @submit="e => emit('submit', e)" class="space-y-4">
        <UFormField label="Nom Fond" name="title">
            <UInput v-model="form.title" class="w-full" />
        </UFormField>
        <UFormField label="Petit description" name="description">
            <UTextarea v-model="form.description" autoresize class="w-full" />
        </UFormField>
        <UFormField label="Type de fond" name="type" required>
            <USelect 
                v-model="form.type" 
                value-key="value" 
                :placeholder="'Sélectionner un type'"
                :items="FUND_TYPE_LIST.map(i => ({value: i, label: getLabelFundType(i)}))" 
                class="w-full" />
        </UFormField>
        <UFormField label="Montant du fond" name="target">
            <UInput v-model="form.target" class="w-full" type="number" />
        </UFormField>


        <UFormField v-if="form.type != FundType.Amortization" 
            label="Compte lie au fond" name="accountId" required>
            <USelect 
                v-model="form.accountId" 
                value-key="value" 
                :items="accounts?.map(i => ({value: i.id, label: i.title}))" 
                class="w-full" />
        </UFormField>

        <div v-if="form.type === FundType.SinkingFund" class="space-y-4">
            <UFormField label="Date de debut" name="startDate" required>
                <UPopover>
                    <UButton color="neutral" variant="subtle" icon="i-lucide-calendar">
                        {{ form.sinkingFundDetail?.startDate ? df.format(parseDate(form.sinkingFundDetail?.startDate)) : 'Selectionner une date' }}
                    </UButton>
                    <template #content>
                        <UCalendar v-model="sinkStartDate" />
                    </template>
                </UPopover>
            </UFormField>

            <UFormField label="Mode de calcul" name="sinkMode" required>
                <USelect v-model="sinkMode" value-key="value" :items="SINK_MODE_ITEMS" class="w-full" />
            </UFormField>

            <div class="grid grid-cols-2 gap-4">
                <UFormField label="Periode de contribution" name="contribPeriod">
                    <USelect v-model="form.sinkingFundDetail!.contributionRecurrence.period" :items="PERIOD_ITEMS" class="w-full" />
                </UFormField>
                <UFormField label="Intervalle de contribution" name="contribInterval">
                    <UInput v-model.number="form.sinkingFundDetail!.contributionRecurrence.interval" :min="1" class="w-full" type="number" />
                </UFormField>
            </div>

            <UFormField v-if="sinkMode === 'contributions'" label="Nombre de contributions" name="numberOfContributions" required>
                <UInput v-model.number="form.sinkingFundDetail!.numberOfContributions" :min="1" class="w-full" type="number" />
            </UFormField>

            <template v-if="sinkMode === 'endDate'">
                <UFormField label="Date de fin" name="schedulerDate" required>
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar">
                            {{ form.sinkingFundDetail?.scheduler?.date ? df.format(parseDate(form.sinkingFundDetail?.scheduler?.date)) : 'Selectionner une date' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="schedulerDate" />
                        </template>
                    </UPopover>
                </UFormField>

                <div>
                    <USwitch label="Est cyclique" 
                        v-model="isCyclic"
                        @update:model-value="(v) => {
                            if (!form) return 

                            if (!form.sinkingFundDetail) return
                            // S'assure que l'objet scheduler existe
                            if (!form.sinkingFundDetail.scheduler) {
                                form.sinkingFundDetail.scheduler = {
                                    date: schedulerDate.toDate(getLocalTimeZone()).toISOString(),
                                    repeater: undefined
                                }
                            }
                            
                            if (v) {
                                form.sinkingFundDetail.scheduler.repeater = {
                                    interval: 1,
                                    period: 'Day'
                                }
                            } else {
                                form.sinkingFundDetail.scheduler.repeater = undefined
                            }
                        }"
                    />
                </div>

                <div v-if="isCyclic && form.sinkingFundDetail?.scheduler?.repeater != null" class="grid grid-cols-2 gap-4">
                    <UFormField label="Periode de prelevement" name="schedPeriod">
                        <USelect v-model="form.sinkingFundDetail.scheduler.repeater.period" :items="PERIOD_ITEMS" class="w-full" />
                    </UFormField>
                    <UFormField label="Intervalle de prelevement" name="schedInterval">
                        <UInput v-model.number="form.sinkingFundDetail.scheduler.repeater.interval" :min="1" class="w-full" type="number" />
                    </UFormField>
                </div>
            </template>
        </div>

        <UButton label="Submit" type="submit" />
    </UForm>
</template>
