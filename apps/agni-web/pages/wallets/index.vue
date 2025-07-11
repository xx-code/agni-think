<script setup lang="ts">
import { computed, ref } from "vue";
import { useFetchResumeAccount, ALL_ACCOUNT_ID, type ResumeAccountType, fetchDeleteAccount} from "../../composables/account";
import { EditAccountModal, EditFreezeTransaction, EditTransactionModal, EditTransferModal } from "#components";
import { fetchListTransaction, fetchTransaction, useFetchListTransactions } from "../../composables/transactions";

const {data: accounts, error: errorAccounts, refresh: refreshAccounts} = useFetchResumeAccount(); 

const selectedAccountId = ref(ALL_ACCOUNT_ID)

const {data: transactions, error: errorTransactions, refresh: refreshTransactions} = useFetchListTransactions({page: 1, limit: 4}) // add compute for change in selected Account

const overlay = useOverlay()
const modalAccount = overlay.create(EditAccountModal, {
    props:{
         onSaved: () => {
            refreshAccounts()
    }},
})

const modalTransfer = overlay.create(EditTransferModal, {
    props: {
        onSaved: async () => {
            refreshAccounts()
            await onUpateAccount(selectedAccountId.value)
        } 
    }
})

const modalTransaction = overlay.create(EditTransactionModal, {
    props: {
        onSaved: async () => {
            refreshAccounts()
            await onUpateAccount(selectedAccountId.value)
        }
    }
})

const modalFreezeTransaction = overlay.create(EditFreezeTransaction, {
    props: {
        onSaved: async () => {
            refreshAccounts()
            await onUpateAccount(selectedAccountId.value)
        }
    } 
})

const onSelectAccount = (id: string) => {
    selectedAccountId.value = id
    onUpateAccount(id)
}
const getAccount = (id: string) => {
    if (accounts.value)
        return accounts.value.find(acc => acc.id === id)
    return null
}

const onEditAccount = (accountId: string|null=null) => {
    modalAccount.patch({ isEdit: accountId !== null, accountId: accountId ? accountId : '' })
    modalAccount.open()
}

const onTransferAccount = (accountId: string = '') => { 
    const filterId = accountId === ALL_ACCOUNT_ID ? '': accountId
    modalTransfer.patch({accountFromId: filterId})
    modalTransfer.open()
}

const onEditTransaction = () => {
    modalTransaction.patch({ accountId: selectedAccountId.value})
    modalTransaction.open()
}

const onEditFreezeTransaction = (accountId: string = '') => {
    modalFreezeTransaction.patch({accountId: accountId})
    modalFreezeTransaction.open()
}

const onDeleteAccount = async (accountId: string) => {
    await fetchDeleteAccount(accountId)
    await refreshAccounts()
}

const onUpateAccount = async (payload: string) => {
    let filterAcc: string[] = []
    if (payload !== ALL_ACCOUNT_ID)
        filterAcc = [payload]
   
    if (transactions.value)
        transactions.value = await fetchListTransaction({page: 1, limit: 4, accountFilter: filterAcc})
}

const listTransaction = computed(() => {
    if (transactions.value)
        return transactions.value.transactions
    return []
}) // TODO Review where there are loop setup loading a

</script>

<template>
    <div class="grid xs:grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-1" style="margin-top: 1rem;">
        <div>
            <div class="card rounded-md">
                <CustomCardTitle :title="getAccount(selectedAccountId)?.title">
                   <USelect v-model="selectedAccountId" @update:modelValue="onUpateAccount" value-key="id"  label-key="title" :items="accounts ? accounts.map(acc => ({ id: acc.id, title: acc.title, type: 'item' })) : []"/> 
                </CustomCardTitle>
                <div class="card-money" style="margin-top: 1rem;">
                    <h2>
                        ${{ getAccount(selectedAccountId)?.balance }}
                    </h2>
                </div>

                <div class="card-bottom" style="margin-top: 1rem;">
                    <div class="flex items-center">
                        <UBadge 
                            variant="subtle"
                            :color="getAccount(selectedAccountId)?.pastBalanceDetail.doIncrease ? 'success' : 'error'"
                            :icon="getAccount(selectedAccountId)?.pastBalanceDetail.doIncrease ? 'i-lucide-arrow-up' : 'i-lucide-arrow-down'">
                            {{ getAccount(selectedAccountId)?.pastBalanceDetail.diffPercent }}%
                        </UBadge>
                        <p>Vs {{ }} precendent</p>
                    </div>
                </div>

                <div class="flex justify-between mt-5">
                    <UButton icon="i-lucide-banknote" size="xl" @click="onEditTransaction()"/>
                    <UButton icon="i-lucide-arrow-right-left" size="xl" @click="onTransferAccount(selectedAccountId)" />
                    <UButton icon="i-lucide-snowflake" size="xl" @click="onEditFreezeTransaction(selectedAccountId)"/>
                </div>
            </div>
        </div>

        <div class="xs:col-1 sm:col-span-1 md:col-span-3 flex flex-col" >
            <div class="self-end" style="margin-bottom: 1rem;">
                <UButton icon="i-lucide-plus" size="xl" variant="solid" @click="onEditAccount(null)">Ajouter Carte</UButton>
            </div>
            <div class="flex overflow-x-auto gap-2"  >
                <div v-for="account in accounts" :key="account.id">
                    <CardResumeAccount 
                        @customClick="onSelectAccount(account.id)"
                        style="width: 200px;"
                        v-if="account.id !== selectedAccountId"
                        :id="account.id"
                        :title="account.title"
                        :balance="account.balance"
                        :diff-past-balance-per="account.pastBalanceDetail.diffPercent"
                        :is-positif="account.pastBalanceDetail.doIncrease"
                        :allow-edit="account.id === ALL_ACCOUNT_ID ? false : true"
                        :allow-delete="account.id === ALL_ACCOUNT_ID ? false :true" 
                        @edit="onEditAccount(account.id)"
                        @delete="onDeleteAccount(account.id)"

                    /> 
                </div>
            </div> 
        </div>
    </div> 

    <div class="grid md:grid-cols-3 gap-2" style="margin-top: 1rem;">
        <div class="card rounded-md col-span-2 ">
            <CustomCardTitle title="Recente transaction">
                <div class="flex gap-1">
                    <!-- <USelect class="rounded-full" v-model="transactionAccountSelected" :items="accounts.map(acc => (acc.title))" /> -->
                    <UButton class="rounded-full" size="sm" label="Voir plus" variant="outline" color="neutral" />
                </div>
            </CustomCardTitle>
            <div class="flex flex-col gap-1" style="margin-top: 1rem;">
                <div v-for="trans in listTransaction" :key="trans.id">
                    <RowTransaction 
                        :id="trans.id" :balance="trans.amount" :title="trans.category.title" 
                        :description="trans.description" :icon="trans.category.icon" :color="trans.category.color"
                        :tags="trans.tags.map(tag=>tag.value)" :date="trans.date" :recordType="trans.recordType"/>    
                </div>
            </div>
        </div>
    
        <div class="card rounded-md md:row-span-2">
            <CustomCardTitle title="Gestion financière responsable">

            </CustomCardTitle>
            <div>
                <p>Graph 50%/30%/20%</p>
            </div>
        </div>

        <div class="card rounded-md col-span-2 ">
            <CustomCardTitle title="Freeze transaction et future transaction">
            </CustomCardTitle>
            <div class="flex flex-col gap-1" style="margin-top: 1rem;">
                <div v-for="trans in listTransaction" :key="trans.id">
                    <RowTransaction 
                        :id="trans.id" :balance="trans.amount" :title="trans.category.title" 
                        :description="trans.description" :icon="trans.category.icon" 
                        :tags="trans.tags.map(tag=>tag.value)" :date="trans.date" :color="trans.category.color"/>    
                        
                </div>
            </div>
        </div>

        
    </div>
</template>

<style scoped lang="scss">
.card {
    border: solid 1px #E6E6E6;
    padding: 0.5rem;
    background: rgb(250, 251, 255);

    .card-money {
        font-weight: bold;
        font-size: x-large;
        color: #1E3050;
    }
    
    .card-bottom {
        p {
            font-size: small;
            margin-left: 2.5px;
            color: #ADADAD;
        }
    }
    
}
</style>