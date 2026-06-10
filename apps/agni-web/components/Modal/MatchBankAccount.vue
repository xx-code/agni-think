<script setup lang="ts">
import { fetchAccounts } from '~/composables/api/accounts';
import { createBankRegister } from '~/composables/api/bankRegister';


const { start, stop } = useLoading()
const { title, accessCode, bankAccounts } = defineProps<{
    title: string
    accessCode: string
    bankAccounts: {id: string, name: string }[]
}>()

const emit = defineEmits<{
    (e: 'close', close: boolean): void
}>();

const bankMatching = ref<{
    name: string
    bankAccountId: string
    accountId: string
}[]>(bankAccounts.map(i => ({ name: i.name, bankAccountId: i.id, accountId: '' })))

const { data: accounts } =  useAsyncData('accounts+all', async () => {
    const res = await fetchAccounts({ queryAll: true, limit: 1, offset: 0})
    return res.items.map(i => ({
        value: i.id,
        label: i.title 
    }))
})



function onSelectBank(bankId: string, id: string) {
    const index = bankMatching.value.findIndex(i => i.bankAccountId === bankId)
    if (index > -1) {
        const accIndex = bankMatching.value.findIndex(i => i.accountId == id)
        if (accIndex < 0) {
            bankMatching.value[index]!.accountId = id
        }
    }
}

async function saveBank() {
    try {
        start()
        if (bankMatching.value.map(i => i.bankAccountId).some(i => i.trim() === ""))
            throw new Error("Aucun account Id selectionner")

        await createBankRegister({
            title: title,
            accessCode: accessCode,
            accounts: bankMatching.value.map(i => ({ accountId: i.accountId, bankAccountId: i.bankAccountId}))
        })
        await $fetch("/api/bank/init-transaction")
        emit("close", true)
    } catch (err) {
        stop()
        alert(err)
        console.log(err)
    } finally {
        stop()
    }
}

</script>

<template>
    <UModal :title="title" :dismissible="false">
        <template #body>
            <div>
                <div v-for="bank in bankMatching" :key="bank.bankAccountId">
                    <div class="flex space-x-2 items-center">
                        <p class="font-bold">{{ bank.name }}</p>
                        <USelect 
                            :items="accounts"
                            placeholder="Select Account"
                            class="w-min-50"
                            value-key="value" 
                            label-key="label"
                            @update:model-value="val => onSelectBank(bank.bankAccountId, val)"
                        />
                    </div>
                </div>
                <div >
                    <UButton label="Enregistrer" @click="saveBank"/>
                </div>
            </div>
        </template> 
    </UModal> 
</template>