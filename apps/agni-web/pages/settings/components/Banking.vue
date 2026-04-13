<script setup lang="ts">
import { ModalMatchBankAccount, UButton, USwitch } from '#components';
import { usePlaidLink, type PlaidLinkOnSuccessMetadata, type PlaidLinkOptions } from '@jcss/vue-plaid-link';
import type { TableColumn } from '@nuxt/ui';
import { fetchAllBankRegister } from '~/composables/bankRegister';

type BankRow = {
    id: string
    name: string
    active: boolean
    numAccount: number
}

const overlay = useOverlay()
const modalMatchBank = overlay.create(ModalMatchBankAccount)

const { data, refresh } = useAsyncData("banking+all+register", async () => {
    const res = await fetchAllBankRegister({ offset: 0, limit:1, queryAll: true })

    return res.items.map(i => ({
        id: i.id,
        name: i.title,
        active: i.active,
        numAccount: i.accounts.length
    } satisfies BankRow))
})

const token = ref<string|null>(null)
const createLink = async () => {
    const res = await $fetch("/api/bank/token", {
        method: 'POST'
    })
    //@ts-ignore
    token.value = res.link_token
}

const propsRegisterBank = ref<{
    accessCode: string,
    title: string,
    bankAccounts: {id: string, name: string }[]
}|undefined>()

const config = computed(() => {
  const config: PlaidLinkOptions = {
    token: token.value,
    onSuccess: async (public_token: string, metadata: PlaidLinkOnSuccessMetadata) => {
        try {
            const res = await $fetch<{code: string}>("/api/bank/exchange-token", {
                method: 'POST',
                body: {
                    public_token: public_token
                }
            })    
            propsRegisterBank.value = {
                accessCode: res.code,
                title: metadata.institution?.name ?? "",
                bankAccounts: metadata.accounts.map(i => ({id: i.id, name: i.name }))
            }
        } catch(err) {
            console.log(err)
        }
    },
  };
  return config;
})


const { start, stop } = useLoading()

async function forceInitTransaction() {
    try {
        start()
        await $fetch("/api/bank/init-transaction")
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
        cell: ({ row }) => {
            return h('div', { }, [
                h(USwitch, { disabled: true, defaultValue: row.original.active })
            ])
        }     
    }, 
    {
        accessorKey: 'numAccount',
        header: 'Number Account'
    },
    {
        accessorKey: 'id',
        cell: ({ row }) => {
            return h('div', {}, [
                h(UButton, { onClick: forceInitTransaction }, "Force Initializaiton transactions")
            ])
        }
    },
]

const { open, ready } = usePlaidLink(config)

async function openModelRegister() {
    const props = propsRegisterBank.value
    if (props) {
        const instant = modalMatchBank.open({
            accessCode: props.accessCode,
            title: props.title,
            bankAccounts: props.bankAccounts
        })

        await instant.result

        propsRegisterBank.value = undefined
        refresh()
    }
} 

onMounted(async () => {
    await createLink()
})

</script>

<template>
    <UCard>
        <div class="space-y-6">
            <div class="flex items-end justify-between">
                <div>
                    <h2 class="text-2xl font-bold tracking-tight text-gray-900">Compte bancaire linker</h2>
                    <p class="text-sm text-gray-500">Gestion des compte bancaire</p>
                </div>

                <div class="flex items-center space-x-2">
                    <UButton 
                        label="Connect Bank" 
                        icon="i-lucide-plus" 
                        size="md"
                        color="primary"
                        @click="open"
                    />
                    <UButton 
                        :disabled="propsRegisterBank === undefined"
                        label="Register Bank" 
                        icon="i-lucide-plus" 
                        size="md"
                        color="secondary"
                        @click="openModelRegister"
                    />
                </div>
                
            </div>

            <UTable  
                :columns="columns"
                :data="data"
            />
        </div>
    </UCard>
</template>