<script setup lang="ts">
import { CalendarDate, getLocalTimeZone, parseDate } from '@internationalized/date';
import type { AccountType } from '~/types/ui/account';

const { accounts, invoiceTypes } = defineProps<{
    accounts: AccountType[]
    invoiceTypes: {label: string, value: string}[]
}>()

const model = defineModel<Partial<{
    accountId: string
    mouvement: string
    state: string
    date: string
    type: string
}>>()

const calendarValue = computed({
    get() {
        if (!model.value) return undefined

        try {
            let valDate = model.value.date ? new Date(model.value.date)  : new Date();
            const date = new CalendarDate(
                valDate.getFullYear(), 
                valDate.getMonth() + 1, 
                valDate.getDate()
            );
            model.value.date = date.toDate(getLocalTimeZone()).toISOString()

            return date
        } catch {
            return undefined
        }
    },
    set(newValue: CalendarDate | undefined) {
        if (!model.value) return

        if (newValue) {
            let valDate = newValue.toDate(getLocalTimeZone())
            model.value.date = valDate.toISOString()
        } else {

            model.value.date = undefined
        }
    }
})

const displayInvoiceType = computed(() => {
    if (!model.value) return []

    if (model.value.mouvement) {
        if (model.value.mouvement === 'Credit')
            return invoiceTypes.filter(i => ["income", "other"].includes(i.value.toLowerCase()))
    }

    return invoiceTypes.filter(i => i.value.toLowerCase() !== 'income')
})

function onInvoiceMouvement() {
    if (!model.value) return

    model.value.type = ""
}

</script>

<template>
    <div class="bg-neutral-50 rounded-lg p-4 space-y-4" v-if="model">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <UFormField label="Mouvement" name="mouvement" required>
                <USelect 
                    v-model="model.mouvement"
                    value-key="value" 
                    v-on:change="onInvoiceMouvement"
                    class="w-full"
                    :items="[{ label: 'Gains', value: 'Credit' }, {label: 'Depense', value: 'Debit' }]" 
                    placeholder="Sélectionner un type"
                    size="lg"
                />
            </UFormField>

            <UFormField label="Type de facture" name="type" required>
                <USelect 
                    v-model="model.type"
                    value-key="value" 
                    :items="displayInvoiceType" 
                    placeholder="Sélectionner un type"
                    size="lg"
                />
            </UFormField>


            <UFormField label="Statut" name="status" required>
                <USelect 
                    v-model="model.state"
                    value-key="value"
                    :items="[
                        { label: 'En attente', value: 'Pending' },
                        { label: 'Validé', value: 'Complete' }
                    ]"
                    size="lg"
                />
            </UFormField>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <UFormField label="Compte" name="accountId" required>
                <USelect 
                    v-model="model.accountId" 
                    value-key="value" 
                    :items="accounts.map(i => ({ value: i.id, label: i.title }))" 
                    placeholder="Sélectionner un compte"
                    size="lg"
                />
            </UFormField>

            <UFormField label="Date" name="date" required>
                <UPopover>
                    <UButton 
                        color="neutral" 
                        variant="outline" 
                        icon="i-lucide-calendar"
                        size="lg"
                        block
                    >
                        {{ calendarValue ? formatDate(calendarValue.toDate(getLocalTimeZone())) : 'Sélectionner une date' }}
                    </UButton>
                    <template #content>
                        <UCalendar v-model="calendarValue"/>
                    </template>
                </UPopover>
            </UFormField>
        </div>
    </div>
</template>