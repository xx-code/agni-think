<template>
    <div class="container">
        <NavBar />
        <UContainer class="content">
            <TopBar />
            <slot />
        </UContainer>
    </div>
</template>

<style scoped>
.container {
    min-height: 100vh;
    display: flex;
}
.content {
    width: 100%;
    padding: 0.5rem;
}
</style>