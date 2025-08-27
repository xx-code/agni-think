<script lang="ts" setup>
import type { FormSubmitEvent } from '#ui/types';
import { CalendarDate, DateFormatter, getLocalTimeZone } from '@internationalized/date';
import * as z from 'zod';
import type { EditSnapshotPatrimony, SnapshotPatrimonyType } from '~/types/ui/patrimony';

const { snapshot } = defineProps<{
    snapshot?: SnapshotPatrimonyType
}>();

const emit = defineEmits<{
    (e: 'submit', value: EditSnapshotPatrimony, oldValue?: SnapshotPatrimonyType): void    
    (e: 'close', close: boolean): void
}>();

const schema = z.object({
    balance: z.number(),
    status: z.string()
})

let date: Date = new Date(); 
if (snapshot?.date)
    date = snapshot.date;

const dated = shallowRef(new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate()));
const df = new DateFormatter('en-Us', {
    dateStyle: 'medium'
});

const form = reactive({
    balance: snapshot?.balance,
    status: snapshot?.status || 'Complete'
})

type Schema = z.output<typeof schema>;

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data
    emit('submit', {
        balance: data.balance,
        status: data.status,
        date: dated.value
    }, snapshot);


    emit('close', true)
}

</script>

<template>
    <UModal title="Ajouter ou diminuer but d'epargne">
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField name="status">
                    <UTabs v-model="form.status" label-key="value" 
                        :content="false" 
                        :items="[{label:'Pending', value: 'Pending'}, {label:'Complete', value: 'Complete'}]" class="w-full"/> 
                </UFormField>

                <UFormField label="Somme actuel" name="balance">
                    <UInput v-model="form.balance" type="number" />
                </UFormField>

                <UFormField label="Date de debut" name="startDate">
                    <UPopover>
                        <UButton color="neutral" variant="subtle" icon="i-lucide-calendar" >
                            {{ dated ? df.format(dated.toDate(getLocalTimeZone()))  : 'Selectionnez une de debut' }}
                        </UButton>
                        <template #content>
                            <UCalendar v-model="dated" />
                        </template>
                    </UPopover>
                </UFormField>

                <UFormField>
                    <UButton label="Envoyer" type="submit"/>
                </UFormField>
            </UForm>
        </template>
    </UModal>
</template>