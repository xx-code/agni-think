<script setup lang="ts">
import { reactive } from "vue";
import type { FormError, FormSubmitEvent } from '@nuxt/ui';
import type { CreatedRequest, ListResponse } from '~/types/api';
import type { GetAccountResponse } from '~/types/api/account';
import { listAccountsToListAccount } from '~/mappers/account';
import type { FundForm } from '~/types/form/fund';
import { fundFormToCreateFundRequest, fundFormToUpdateFundRequest } from "~/mappers/fund";
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';

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
    const res = await ApiLinkBuilder
        .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
        .query(query)
        .mapper(listAccountsToListAccount)
        .execute()
    isloading.value = false

    return res.items
})


async function onSubmit(event: FormSubmitEvent<FundForm>) {
    const data = event.data;
    isloading.value = true
    try {
        let id = fundId
        if (fundId && initData) {
            await ApiLinkBuilder
                .route(API_ROUTES.FUNDS.UPDATE_FUND)
                .params({ id: fundId })
                .body(fundFormToUpdateFundRequest(data))
                .execute()
        } else {
            const res = await ApiLinkBuilder
                .route<CreatedRequest>(API_ROUTES.FUNDS.CREATE_FUND)
                .body(fundFormToCreateFundRequest(data))
                .execute()
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