<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import useConfirmModal from '~/composables/modal/useConfirmModal';
import { useSpendingPeriodTemplateModal } from '~/composables/modal/spendingPeriodTemplate';
import { profileResponseToProfile } from '~/mappers/profile';
import { listSpendingPeriodTemplateResponseToListSpendingPeriodTemplate } from '~/mappers/spending-period-template';
import { API_ROUTES } from '~/shared/routes';
import type { ListResponse } from '~/types/api';
import type { GetInternalTypeResponse } from '~/types/api/internal';
import type { GetProfileResponse, UpdateProfileRequest } from '~/types/api/profile';
import type { GetSpendingPeriodTemplateResponse } from '~/types/api/spending-period-template';
import type { Profile } from '~/types/ui/profile';
import type { SpendingPeriodTemplate } from '~/types/ui/spending-period-template';

const overlay = useOverlay()
const toast = useToast()
const isLoading = ref(false)
const updatingTemplateId = ref<string | null>(null)

const { open: openConfirm } = useConfirmModal(overlay)
const { open: openTemplate } = useSpendingPeriodTemplateModal(overlay)

const { data: profile, refresh: refreshProfile } = useAsyncData('settings+profile', async () => {
    return await ApiLinkBuilder
        .route<GetProfileResponse>(API_ROUTES.PROFILE.GET_PROFILE)
        .params({ id: "457ae73e-8124-4d3b-ab2b-d6a404c6b4d3" })
        .mapper(profileResponseToProfile)
        .execute()
})

const { data: spendingPeriodTemplates, refresh: refreshSpendingPeriodTemplates } = useAsyncData('settings+spending-period-templates', async () => {
    const res = await ApiLinkBuilder
        .route<ListResponse<GetSpendingPeriodTemplateResponse>>(API_ROUTES.SPENDING_PERIOD_TEMPLATES.GET_ALL_SPENDING_PERIOD_TEMPLATE)
        .query({ queryAll: true, limit: 0, offset: 0 })
        .mapper(listSpendingPeriodTemplateResponseToListSpendingPeriodTemplate)
        .execute()

    return res.items
})

const { data: periodTypes } = useAsyncData('spending-period-template+period-types', async () => {
    return await ApiLinkBuilder
        .route<GetInternalTypeResponse[]>(API_ROUTES.INTERNALS.PERIOD_TYPE)
        .execute()
})

const form = reactive<Profile>({
    maxWishlistAmount: 0,
    fixSpendPercentage: 0,
    varialSpendPercentage: 0,
    savingPercentage: 0
})

watch(profile, (value) => {
    if (value)
        Object.assign(form, value)
}, { immediate: true })

const totalPercentage = computed(() => form.fixSpendPercentage + form.varialSpendPercentage + form.savingPercentage)

function validate(state: Profile): FormError[] {
    const errors = []

    if (state.maxWishlistAmount < 0)
        errors.push({ name: 'maxWishlistAmount', message: 'Le montant doit etre positif' })

    if (state.fixSpendPercentage < 0 || state.fixSpendPercentage > 100)
        errors.push({ name: 'fixSpendPercentage', message: 'Le pourcentage doit etre compris entre 0 et 100' })

    if (state.varialSpendPercentage < 0 || state.varialSpendPercentage > 100)
        errors.push({ name: 'varialSpendPercentage', message: 'Le pourcentage doit etre compris entre 0 et 100' })

    if (state.savingPercentage < 0 || state.savingPercentage > 100)
        errors.push({ name: 'savingPercentage', message: 'Le pourcentage doit etre compris entre 0 et 100' })

    if (state.fixSpendPercentage + state.varialSpendPercentage + state.savingPercentage > 100) {
        errors.push({ name: 'fixSpendPercentage', message: 'La somme des pourcentages doit etre inferieure a 100' })
        errors.push({ name: 'varialSpendPercentage', message: 'La somme des pourcentages doit etre inferieure a 100' })
        errors.push({ name: 'savingPercentage', message: 'La somme des pourcentages doit etre inferieure a 100' })
    }

    return errors
}

async function onSubmitProfile(event: FormSubmitEvent<Profile>) {
    const data = event.data

    const body: UpdateProfileRequest = {
        maxWishlistAmount: data.maxWishlistAmount,
        fixSpendPercentage: data.fixSpendPercentage,
        varialSpendPercentage: data.varialSpendPercentage,
        savingPercentage: data.savingPercentage
    }

    try {
        isLoading.value = true

        await ApiLinkBuilder
            .route(API_ROUTES.PROFILE.UPDATE_PROFILE)
            .params({ id: "457ae73e-8124-4d3b-ab2b-d6a404c6b4d3" })
            .body(body)
            .execute()

        refreshProfile()

        toast.add({
            title: "Profile updated",
            description: "Vos regles de repartition ont ete mises a jour",
            color: 'success'
        });
    } catch(err: any) {
        toast.add({
            title: "Error update profile",
            description: err?.message,
            color: 'error'
        });
    } finally {
        isLoading.value = false
    }
}

function recurrenceLabel(template: SpendingPeriodTemplate) {
    const period = periodTypes.value?.find(t => t.id === template.recurrence.period)?.value ?? template.recurrence.period

    return `${template.recurrence.interval} x ${period}`
}

async function onToggleTemplate(template: SpendingPeriodTemplate) {
    try {
        updatingTemplateId.value = template.id

        await ApiLinkBuilder
            .route(API_ROUTES.SPENDING_PERIOD_TEMPLATES.PUT_SPENDING_PERIOD_TEMPLATE)
            .params({ id: template.id })
            .body({ isActive: !template.isActive })
            .execute()

        refreshSpendingPeriodTemplates()
    } catch(err: any) {
        toast.add({
            title: "Error update spending period template",
            description: err?.message,
            color: 'error'
        });
    } finally {
        updatingTemplateId.value = null
    }
}

function onDeleteTemplate(id: string, recurrence: string) {
    openConfirm({
        title: `Voulez vous supprimer le modele ${recurrence}?`,
        description: ''
    }, async () => {
        try {
            await ApiLinkBuilder
                .route(API_ROUTES.SPENDING_PERIOD_TEMPLATES.DELETE_SPENDING_PERIOD_TEMPLATE)
                .params({ id })
                .execute()

            refreshSpendingPeriodTemplates()
        } catch(err: any) {
            toast.add({
                title: "Error delete spending period template",
                description: err?.message,
                color: 'error'
            });
        }
    })
}
</script>

<template>
    <div class="space-y-6">
        <UiCard>
            <div class="space-y-6">
                <div>
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Profile</h2>
                    <p class="text-sm text-gray-500">Regles de repartition de vos depenses et de votre epargne</p>
                </div>

                <UForm :validate="validate" :state="form" @submit="onSubmitProfile" class="space-y-4">
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <UFormField label="Montant maximum de wishlist" name="maxWishlistAmount">
                            <UInput type="number" v-model="form.maxWishlistAmount" />
                        </UFormField>

                        <UFormField label="Epargne (%)" name="savingPercentage">
                            <UInput type="number" v-model="form.savingPercentage" />
                        </UFormField>

                        <UFormField label="Depenses fixes (%)" name="fixSpendPercentage">
                            <UInput type="number" v-model="form.fixSpendPercentage" />
                        </UFormField>

                        <UFormField label="Depenses variables (%)" name="varialSpendPercentage">
                            <UInput type="number" v-model="form.varialSpendPercentage" />
                        </UFormField>
                    </div>

                    <div class="flex items-center gap-3">
                        <UButton label="Enregistrer" type="submit" :loading="isLoading" />
                        <span class="text-xs text-gray-500">Total reparti : {{ totalPercentage }}%</span>
                    </div>
                </UForm>
            </div>
        </UiCard>

        <UiCard>
            <div class="space-y-6">
                <div class="flex items-end justify-between">
                    <div>
                        <h2 class="text-2xl font-bold tracking-tight text-gray-900">Modeles de periode de depense</h2>
                        <p class="text-sm text-gray-500">Recurrence de generation de vos periodes de depense</p>
                    </div>
                    <UButton
                        label="Nouveau modele"
                        icon="i-lucide-plus"
                        size="md"
                        color="primary"
                        @click="openTemplate(refreshSpendingPeriodTemplates)"
                    />
                </div>

                <div class="overflow-hidden border border-gray-200 rounded-xl">
                    <table class="min-w-full divide-y divide-gray-200">
                        <thead class="bg-gray-50">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Recurrence
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Budgets
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Date de debut
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Date de fin
                                </th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actif
                                </th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                                    Actions
                                </th>
                            </tr>
                        </thead>
                        <tbody class="bg-white divide-y divide-gray-200">
                            <tr v-for="template of spendingPeriodTemplates"
                                :key="template.id"
                                class="hover:bg-gray-50 transition-colors">
                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="flex items-center gap-3">
                                        <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-blue-50">
                                            <UIcon
                                                name="i-lucide-repeat"
                                                class="w-4 h-4 text-blue-600"
                                            />
                                        </div>
                                        <span class="text-sm font-medium text-gray-900">
                                            {{ recurrenceLabel(template) }}
                                        </span>
                                    </div>
                                </td>

                                <td class="px-6 py-4">
                                    <div v-if="template.budgets.length > 0" class="flex flex-wrap gap-1.5">
                                        <span v-for="budget of template.budgets"
                                            :key="budget.id"
                                            class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                                            {{ budget.title }}
                                        </span>
                                    </div>
                                    <span v-else class="text-xs text-gray-400">Tous les budgets</span>
                                </td>

                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-gray-600">
                                        {{ template.startDate.toLocaleDateString('fr-CA') }}
                                    </span>
                                </td>

                                <td class="px-6 py-4 whitespace-nowrap">
                                    <span class="text-sm text-gray-600">
                                        {{ template.endDate ? template.endDate.toLocaleDateString('fr-CA') : 'Illimitee' }}
                                    </span>
                                </td>

                                <td class="px-6 py-4 whitespace-nowrap">
                                    <div class="flex items-center gap-2">
                                        <USwitch :model-value="template.isActive"
                                            :disabled="updatingTemplateId === template.id"
                                            @update:model-value="onToggleTemplate(template)" />
                                        <span v-if="template.isActive"
                                            class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                            Actif
                                        </span>
                                        <span v-else
                                            class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">
                                            Inactif
                                        </span>
                                    </div>
                                </td>

                                <td class="px-6 py-4 whitespace-nowrap text-right">
                                    <div class="flex items-center justify-end gap-2">
                                        <UButton
                                            variant="ghost"
                                            color="neutral"
                                            icon="i-lucide-pencil"
                                            size="xs"
                                            @click="openTemplate(refreshSpendingPeriodTemplates, template.id)"
                                        />
                                        <UButton
                                            variant="ghost"
                                            color="error"
                                            icon="i-lucide-trash-2"
                                            size="xs"
                                            @click="onDeleteTemplate(template.id, recurrenceLabel(template))"
                                        />
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div v-if="!spendingPeriodTemplates || spendingPeriodTemplates.length === 0"
                    class="text-center py-12 bg-gray-50 rounded-xl border-2 border-dashed border-gray-200">
                    <UIcon name="i-lucide-repeat" class="w-12 h-12 text-gray-300 mx-auto mb-3" />
                    <p class="text-gray-500 text-sm">Aucun modele de periode configure</p>
                    <UButton
                        label="Creer le premier modele"
                        size="sm"
                        class="mt-3"
                        @click="openTemplate(refreshSpendingPeriodTemplates)"
                    />
                </div>
            </div>
        </UiCard>
    </div>
</template>
