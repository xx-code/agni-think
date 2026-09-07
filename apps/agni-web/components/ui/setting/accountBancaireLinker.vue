<script setup lang="ts">
import { UButton, USwitch } from '#components';
import { usePlaidLink } from '@jcss/vue-plaid-link';
import type { TableColumn } from '@nuxt/ui';
import { listBankRegistersResponseToListBankRegisters } from '~/mappers/bankRegister';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import { useLinkAccountWithBankModal } from '~/composables/modal/bankLinker';

type BankRow = {
    id: string
    name: string
    active: boolean
    numAccount: number
}

const overlay = useOverlay()
const { start, stop } = useLoading()

const { data, refresh } = useAsyncData("banking+all+register", async () => {
    const res = await ApiLinkBuilder
        .route(API_ROUTES.BANK_REGISTERS.GET_BANK_REGISTERS)
        .query({ offset: 0, limit:1, queryAll: true })
        .mapper(listBankRegistersResponseToListBankRegisters).execute()

    return res.items.map(i => ({
        id: i.id,
        name: i.title,
        active: i.active,
        numAccount: i.accounts.filter(i => i.isActive).length
    } satisfies BankRow))
})

const { isReady, createLink, config } = useBankLinker(refresh)
const { open: openBankModel } = useLinkAccountWithBankModal(overlay)

const { open } = usePlaidLink(config)

async function forceInitTransaction() {
    try {
        start()
        await ApiLinkBuilder
            .route(API_ROUTES.BANK.INIT_TRANSACTION)
            .execute()
        stop()
    } catch(err) {
        stop()
        console.log(err)
        alert(err)
    }
}

const columns: TableColumn<BankRow>[] = [
    {
        accessorKey: 'name',
        header: 'Name',
    },
    {
        accessorKey: 'active',
        header: 'En Activite',
        cell: ({ row }) => {
            return h('div', { }, [
                h(USwitch, { disabled: true, defaultValue: row.original.active })
            ])
        }     
    }, 
    {
        accessorKey: 'numAccount',
        header: 'Nombre de compte actif'
    },
    {
        accessorKey: 'id',
        header: '',
        cell: ({ row }) => {
            return h('div', { class: 'flex items-center gap-2'}, [
                h(UButton, { onClick: () => openBankModel(refresh) }, "Lien compte bancaire"),
                h(UButton, { onClick: forceInitTransaction }, "Force Initializaiton transactions")
            ])
        }
    },
]

onMounted(() => {
    createLink()
})

</script>

<template>
    <UiCard>
        <div class="space-y-6">
            <div class="flex items-end justify-between">
                <div>
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Compte bancaire linker</h2>
                    <p class="text-sm text-gray-500">Gestion des compte bancaire</p>
                </div>

                <div class="flex items-center space-x-2">
                    <UButton 
                        v-if="isReady"
                        label="Connect Bank" 
                        icon="i-lucide-plus" 
                        size="md"
                        color="primary"
                        @click="open"
                    />
                </div>
                
            </div>

            <UTable  
                :columns="columns"
                :data="data"
            />
        </div>
    </UiCard>
</template>