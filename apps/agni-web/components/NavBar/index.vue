<script setup lang="ts">

</script>

<template>
    <div class="nav-container">
        
        <div class="nav-content">
            <div class="top-nav">
                <NavBarLogoTitle title=".Agin" />
            </div>
            <div>
                <NavBarItem title="Dashboard" link="/" icon="" />
                <NavBarItem title="Transactions" link="/transactions" icon="" />
                <NavBarItem title="Wallets" link="/wallets" icon="" />
                <NavBarItem title="Budgets" link="/budgtes" icon="" />
                <NavBarItem title="Saving" link="/savings" icon="" />
                <NavBarItem title="Analystics" link="/analysts" icon="" />
                <NavBarItem title="Settings" link="/settings" icon="" />
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