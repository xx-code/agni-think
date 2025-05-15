<script setup lang="ts">
import {useWindowSize} from '@vueuse/core'
import { computed } from 'vue'

const {width, height} = useWindowSize()
const isResponsive = computed(() => width.value <= 975)
</script>

<template>
    <div class="nav-container">
        <div class="nav-content">
            <div class="top-nav">
                <NavBarLogoTitle :title="isResponsive ? 'A.' : 'Agni.'" />
            </div>
            <div>
                <NavBarItem title="Dashboard" link="/" icon="fa-solid fa-house" :isResponsive="isResponsive" />
                <NavBarItem title="Transactions" link="/transactions" icon="fa-solid fa-money-bill-transfer" :isResponsive="isResponsive"/>
                <NavBarItem title="Wallets" link="/wallets" icon="fa-solid fa-wallet" :isResponsive="isResponsive" />
                <NavBarItem title="Budgets" link="/budgets" icon="fa-solid fa-coins" :isResponsive="isResponsive" />
                <NavBarItem title="Goals" link="/goals" icon="fa-solid fa-bullseye" :isResponsive="isResponsive" />
                <NavBarItem title="Analystics" link="/analystics" icon="fa-solid fa-chart-line" :isResponsive="isResponsive"/>
                <NavBarItem title="Settings" link="/settings" icon="fa-solid fa-gear" :isResponsive="isResponsive" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.top-nav {
    margin-bottom: 75px;
    margin-left: 1em;
    margin-right: 1em;
    transition: 250ms ease-out;
}
.nav-container {
    background-color: rgba(103, 85, 215, 0.1);
    width: 225px;
    @media (max-width: 975px) {
        width: 75px;
    };
    transition: 250ms ease-out;
}
.nav-content {
    padding: 1rem;
}
</style>