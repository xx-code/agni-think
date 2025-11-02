<script setup lang="ts">

</script>

<template>
    <div class="nav-container">
        <div class="nav-content">
            <div class="top-nav">
                <NavBarLogoTitle title="Agni." title-responsive="A." />
            </div>
            <div>
                <NavBarItem title="Dashboard" link="/" icon="i-lucide-circle-gauge"  />
                <NavBarItem title="Transactions" link="/transactions" icon="i-lucide-banknote" />
                <NavBarItem title="Wallets" link="/wallets" icon="i-lucide-wallet-minimal" />
                <NavBarItem title="Schedule Transactions" link="/scheduletransactions" icon="i-lucide-banknote" />
                <NavBarItem title="Budgets" link="/budgets" icon="i-lucide-wallet-cards"  />
                <NavBarItem title="Goals" link="/goals" icon="i-lucide-piggy-bank"  />
                <NavBarItem title="Analystics" link="/analystics" icon="i-lucide-chart-network"/>
                <NavBarItem title="Patrimoine" link="/patrimonies" icon="i-lucide-castle" />
                <NavBarItem title="Settings" link="/settings" icon="i-lucide-settings"/>
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
    background-color: #feffff;
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