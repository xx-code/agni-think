<script setup lang="ts">
import {useWindowSize} from '@vueuse/core'
import { computed, ref, watch, watchEffect } from 'vue'

const {width, height} = useWindowSize()
const isResponsive = ref(false)
watchEffect(() => {
    if (width.value <= 975)
        isResponsive.value = true
    else
        isResponsive.value = false
})
</script>

<template>
    <div class="nav-container">
        <div class="nav-content">
            <div class="top-nav">
                <NavBarLogoTitle :title="isResponsive ? 'A.' : 'Agni.'" />
            </div>
            <div>
                <NavBarItem title="Dashboard" link="/" icon="i-lucide-circle-gauge" :isResponsive="isResponsive" />
                <NavBarItem title="Transactions" link="/transactions" icon="i-lucide-banknote" :isResponsive="isResponsive"/>
                <NavBarItem title="Wallets" link="/wallets" icon="i-lucide-wallet-minimal" :isResponsive="isResponsive" />
                <NavBarItem title="Budgets" link="/budgets" icon="i-lucide-wallet-cards" :isResponsive="isResponsive" />
                <NavBarItem title="Goals" link="/goals" icon="i-lucide-piggy-bank" :isResponsive="isResponsive" />
                <NavBarItem title="Analystics" link="/analystics" icon="i-lucide-chart-network" :isResponsive="isResponsive"/>
                <NavBarItem title="Settings" link="/settings" icon="i-lucide-settings" :isResponsive="isResponsive" />
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.top-nav {
    margin-bottom: 75px;
    margin-left: 1em;
    margin-right: 1em;
    transition: 250ms ease-out;
}
.nav-container {
    background-color: rgba(103, 85, 215, 0.1);
    width: 225px;
    transition: 250ms ease-out;
    @media (max-width: 975px) {
        width: 75px;
    };
}
.nav-content {
    padding: 1rem;
}
</style>