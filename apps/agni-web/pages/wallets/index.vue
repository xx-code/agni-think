<script  setup lang="ts">
import { ref } from "vue";
import { useFetchResumeAccount, ALL_ACCOUNT_ID} from "../../composables/account";

const accounts = useFetchResumeAccount();
const selectedAccountId = ref(ALL_ACCOUNT_ID);
</script>

<template>
    <div class="grid grid-cols-4">
        <div>

        </div>
        <div class="col-span-3 flex flex-col" style="width: 100%;">
            <div class="self-end" style="margin-bottom: 1rem;">
                <UButton icon="i-lucide-plus" size="xl" variant="solid">Ajouter Carte</UButton>
            </div>
            <div class="flex overflow-x-auto gap-2"  >
                <div v-for="account in accounts" :key="account.id">
                    <CardResumeAccount 
                        style="width: 200px;"
                        v-if="account.id !== selectedAccountId"
                        :id="account.id"
                        :title="account.title"
                        :balance="account.balance"
                        :diff-past-balance-per="account.pastBalanceDetail.diffPercent"
                        :is-positif="account.pastBalanceDetail.doIncrease"
                        :allow-edit="true"
                        :allow-open="true"
                    /> 
                </div>
            </div> 
        </div>
    </div> 
</template>

<style scoped>

</style>