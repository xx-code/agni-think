<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import { fetchAccounts } from '~/composables/api/accounts';
import type { FundForm } from '~/types/form/fund';
import { fundFormToCreateFundRequest, fundFormToUpdateFundRequest } from "~/mappers/fund";
import { useCreateFund, useUpdateFund } from "~/composables/api/funds";

const { fundId, initData } = defineProps<{
    fundId?: string
    initData?: FundForm
}>();
const emit = defineEmits<{
    (e: 'close', returnId?: string): void
}>();
const toast = useToast()

const isloading = ref(false)
const form = reactive<Partial<FundForm>>({...initData});

function validate(state: Partial<FundForm>): FormError[] {
    const errors: FormError[] = []

    if (!state.title) 
        errors.push({name: 'title', message: 'Require'})

    if (state.title && state.title.trim() == "")
        errors.push({name: 'title', message: 'Vous devez ajouter un titre'})

    if (!state.target) 
        errors.push({name: 'targetAmount', message: 'Require'})

    if (state.target && state.target <= 0)
        errors.push({name: 'targetAmount', message: 'La somme d\'argent doit etre superieux a zero'})

    return errors
}

const { data: accounts } = useAsyncData('modal+fund+accounts', async () => {
    isloading.value = true
    const query = { offset: 0, limit: 0, queryAll: true }
    const accounts = await fetchAccounts(query)
    isloading.value = false

    return accounts.items
})


async function onSubmit(event: FormSubmitEvent<FundForm>) {
    const data = event.data;
    isloading.value = true
    try {
        let id = fundId
        if (fundId && initData) 
            await useUpdateFund(fundId, fundFormToUpdateFundRequest(data))
        else {
            const res = await useCreateFund(fundFormToCreateFundRequest(data))
            id = res.newId
        } 
        
        toast.add({
            title: 'Succès',
            description: fundId ? 'Objectif mis à jour' : 'Objectif créé',
            color: 'success'
        })

        emit('close', id);
    } catch(err: any) {
        toast.add({
            title: err?.error,
            description: err?.message,
            color: 'error'
        })
    } finally {
        isloading.value = false
    }
}

</script>

<template>
    <UModal :title="fundId ? form.title : 'Editeur de Fond'" >
        <template #body>
            <FormFund 
                v-model="form"
                :accounts="accounts ?? []"
                :validate="validate"
                @submit="onSubmit"
            />
        </template>
    </UModal>
</template>