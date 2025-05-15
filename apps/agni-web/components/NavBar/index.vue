<script setup lang="ts">

</script>

<template>
    <div class="nav-container">
        
        <div class="nav-content">
            <div class="top-nav">
                <NavBarLogoTitle title=".Agin" />
            </div>
            <div>
                <NavBarItem title="Dashboard" link="/" icon="fa-solid fa-house" />
                <NavBarItem title="Transactions" link="/transactions" icon="fa-solid fa-money-bill-transfer" />
                <NavBarItem title="Wallets" link="/wallets" icon="fa-solid fa-wallet" />
                <NavBarItem title="Budgets" link="/budgets" icon="fa-solid fa-coins" />
                <NavBarItem title="Goals" link="/goals" icon="fa-solid fa-bullseye" />
                <NavBarItem title="Analystics" link="/analystcs" icon="fa-solid fa-chart-line" />
                <NavBarItem title="Settings" link="/settings" icon="fa-solid fa-gear" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.top-nav {
    margin-bottom: 75px;
    margin-left: 1em;
    margin-right: 1em;
}
.nav-container {
    background-color: rgba(103, 85, 215, 0.1);
    width: 225px;
}
.nav-content {
    padding: 1rem;
}
</style>