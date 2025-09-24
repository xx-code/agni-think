<script lang="ts" setup>
import type { FormSubmitEvent } from '#ui/types';
import * as z from 'zod';
import useAccounts from '~/composables/accounts/useAccounts';
import type { EditePatrimony, PatrimonyDetailType, PatrimonyType, TypePatrimony } from '~/types/ui/patrimony';

const { patrimony } = defineProps<{
    patrimony?: PatrimonyDetailType
}>()

const emit = defineEmits<{
    (e: 'submit', value: EditePatrimony, oldValue?: PatrimonyDetailType): void    
    (e: 'close', close: boolean): void
}>();

const schema = z.object({
    title: z.string().nonempty('Vous devez ajouter un titre'),
    amount: z.number(),
    // description: z.string().optional(),
    // category: z.string(),
    type: z.string(),
})

const {data: accounts} = useAccounts({
    limit: 0,
    offset: 0,
    queryAll: true
})

type Schema = z.output<typeof schema>;

const form = reactive({
    title: patrimony?.title || '',
    amount: patrimony?.amount || 0,
    // description: patrimony?.description || '',
    type: patrimony?.type,
    // categoryId: patrimony?.id
});

const selectedAccountId = ref<string[]>([])

async function onSubmit(event: FormSubmitEvent<Schema>) {
    const data = event.data;
    emit('submit', { 
        title: data.title,
        amount: data.amount,
        description: '',
        categoryId: '',
        accountIds: selectedAccountId.value,
        type: data.type as TypePatrimony
    }, patrimony);
    
    emit('close', false);
}

</script>

<template>
    <UModal title="Editeur de patrimoine" >
        <template #body>
            <UForm :schema="schema" :state="form" @submit="onSubmit" class="space-y-4">
                <UFormField label="Nom du patrimonie" name="title">
                    <UInput v-model="form.title" class="w-full" />
                </UFormField>
                <!-- <UFormField label="Petit description d'epargne" name="description">
                    <UTextarea v-model="form.description" autoresize class="w-full" />
                </UFormField> --> 

                <UFormField label="Type de patrimoine" name="type">
                    <URadioGroup 
                        orientation="horizontal" 
                        variant="list" 
                        :items="[{label: 'Actif', value: 'Asset'}, {label: 'Passif', value: 'Liability'}]" 
                        value-key="value"
                        v-model="form.type" />
                </UFormField>

                <UFormField label="Valeur du patrimoine" name="amount">
                    <UInput v-model="form.amount" class="w-full" type="number" />
                </UFormField>

                <UFormField label="Selectionner les comptes cible">
                    <UInputMenu 
                        multiple
                        v-model="selectedAccountId" 
                        value-key="value"
                        :items="accounts?.items.map(i => ({ label: i.title, value: i.id }))" />
                </UFormField> 

                <div>
                    <UButton label="Submit" type="submit" />
                </div>
            </UForm>
        </template>
    </UModal>
</template>