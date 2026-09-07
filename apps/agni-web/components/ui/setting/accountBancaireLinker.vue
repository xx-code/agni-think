<script setup lang="ts">
import { UButton, USwitch, USelectMenu } from '#components';
import { usePlaidLink } from '@jcss/vue-plaid-link';
import type { TableColumn } from '@nuxt/ui';
import { listAccountsToListAccount } from '~/mappers/account';
import { listBankRegistersResponseToListBankRegisters } from '~/mappers/bankRegister';
import { ApiLinkBuilder } from '~/utils/ApiLinkBuilder';
import { API_ROUTES } from '~/shared/routes';
import type { ListResponse } from '~/types/api';
import type { GetAccountResponse } from '~/types/api/account';
import type { UpdateBankRegisterRequest } from '~/types/api/bank-register';

type BankAccountRow = {
    bankAccountId: string
    bankName: string
    accountId?: string
    isActive: boolean
}

type BankRow = {
    id: string
    name: string
    active: boolean
    numAccount: number
    accounts: BankAccountRow[]
}

const toast = useToast()
const { start, stop } = useLoading()
const expanded = ref<Record<string, boolean>>({})

const { data: appAccounts } = useAsyncData("banking+app+accounts", async () => {
    const res = await ApiLinkBuilder
        .route<ListResponse<GetAccountResponse>>(API_ROUTES.ACCOUNTS.GET_ACCOUNTS)
        .query({ offset: 0, limit: 1, queryAll: true })
        .mapper(listAccountsToListAccount)
        .execute()
    return res.items
})

const { data, refresh } = useAsyncData("banking+all+register", async () => {
    const res = await ApiLinkBuilder
        .route(API_ROUTES.BANK_REGISTERS.GET_BANK_REGISTERS)
        .query({ offset: 0, limit: 1, queryAll: true })
        .mapper(listBankRegistersResponseToListBankRegisters)
        .execute()

    return res.items.map(i => ({
        id: i.id,
        name: i.title,
        active: i.active,
        numAccount: i.accounts.filter(a => a.isActive).length,
        accounts: i.accounts.map(a => ({
            bankAccountId: a.bankAccountId,
            bankName: a.bankName,
            accountId: a.accountId ?? undefined,
            isActive: a.isActive
        }))
    } satisfies BankRow))
})

const accountOptions = computed(() => {
    return  [
        { label: 'Aucun', value: undefined },
        ...(appAccounts.value?.map(a => ({ label: a.title, value: a.id })) ?? [])
    ]
})

const { isReady, createLink, config } = useBankLinker(refresh)
const { open } = usePlaidLink(config)

async function forceInitTransaction() {
    try {
        start()
        await ApiLinkBuilder.route(API_ROUTES.BANK.INIT_TRANSACTION).execute()
        toast.add({
            title: "Transaction initialiser",
            color: 'success'
        })
    } catch (err) {
        console.log(err)
        alert(err)
    } finally {
        stop()
    }
}

async function persistAccounts(bankRegisterId: string, accounts: BankAccountRow[]) {
    await ApiLinkBuilder
        .route(API_ROUTES.BANK_REGISTERS.UPDATE_BANK_REGISTER)
        .params({ id: bankRegisterId })
        .body({
            accounts: accounts.map(a => ({
                accountId: a.accountId ?? null,
                bankName: a.bankName,
                bankAccountId: a.bankAccountId
            }))
        } as UpdateBankRegisterRequest)
        .execute()
}

function findAccount(registerId: string, bankAccountId: string) {
    const register = data.value?.find(r => r.id === registerId)
    if (!register) return null
    const index = register.accounts.findIndex(a => a.bankAccountId === bankAccountId)
    if (index < 0) return null
    return { register, index }
}

async function onToggleActive(registerId: string, bankAccountId: string, value: boolean) {
    const found = findAccount(registerId, bankAccountId)
    if (!found) return
    const { register, index } = found
    const previous = register.accounts[index]!.isActive
    register.accounts[index]!.isActive = value

    try {
        start()
        await persistAccounts(registerId, register.accounts)
    } catch (err: any) {
        register.accounts[index]!.isActive = previous
        toast.add({ title: err?.error, description: err?.message, color: 'error' })
    } finally {
        stop()
    }
}

async function onChangeLinkedAccount(registerId: string, bankAccountId: string, newAccountId?: string) {
    const found = findAccount(registerId, bankAccountId)
    if (!found) return
    const { register, index } = found
    const previous = register.accounts[index]!.accountId
    register.accounts[index]!.accountId = newAccountId

    try {
        start()
        await persistAccounts(registerId, register.accounts)
        await ApiLinkBuilder.route(API_ROUTES.BANK.INIT_TRANSACTION).execute()
        await refresh()
    } catch (err: any) {
        register.accounts[index]!.accountId = previous
        toast.add({ title: err?.error, description: err?.message, color: 'error' })
    } finally {
        stop()
    }
}

const columns: TableColumn<BankRow>[] = [
    {
        id: 'expand',
        cell: ({ row }) =>
            h(UButton, {
                color: 'neutral',
                variant: 'ghost',
                icon: 'i-lucide-chevron-down',
                square: true,
                'aria-label': 'Expand',
                ui: { leadingIcon: [row.getIsExpanded() ? 'rotate-180' : '', 'transition-transform'].join(' ') },
                onClick: () => row.toggleExpanded()
            })
    },
    {
        accessorKey: 'name',
        header: 'Name',
    },
    {
        accessorKey: 'active',
        header: 'En Activite',
        cell: ({ row }) => h('div', {}, [
            h(USwitch, { disabled: true, defaultValue: row.original.active })
        ])
    },
    {
        accessorKey: 'numAccount',
        header: 'Nombre de compte actif'
    },
    {
        accessorKey: 'id',
        header: '',
        cell: ({ row }) => h('div', { class: 'flex items-center gap-2' }, [
            h(UButton, { onClick: forceInitTransaction }, "Force Initializaiton transactions")
        ])
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
                v-model:expanded="expanded"
            >
                <template #expanded="{ row }">
                    <div class="space-y-2 py-2 pl-10">
                        <div
                            v-for="account in row.original.accounts"
                            :key="account.bankAccountId"
                            class="flex items-center gap-4 border-b border-gray-100 py-2 last:border-none"
                        >
                            <span class="w-48 truncate text-sm font-medium">{{ account.bankName }}</span>

                            <USelectMenu
                                class="w-56"
                                :items="accountOptions"
                                value-key="value"
                                :model-value="account.accountId"
                                @update:model-value="(v: string | undefined) => onChangeLinkedAccount(row.original.id, account.bankAccountId, v)"
                            />

                            <!-- <USwitch
                                :model-value="account.isActive"
                                @update:model-value="(v: boolean) => onToggleActive(row.original.id, account.bankAccountId, v)"
                            /> -->
                        </div>

                        <p v-if="!row.original.accounts.length" class="text-sm text-gray-400">
                            Aucun compte bancaire pour cette institution.
                        </p>
                    </div>
                </template>
            </UTable>
        </div>
    </UiCard>
</template>