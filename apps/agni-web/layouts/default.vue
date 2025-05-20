<template>
    <UApp>
        <div class="extent-container">
            <NavBar />
            <UContainer class="content">
                <TopBar />
                <slot />
            </UContainer>
        </div>
    </UApp>
    
</template>

<style scoped>
.extent-container {
    min-height: 100vh;
    display: flex;
}
.content {
    width: 100%;
    padding: 0.5rem;
}
</style>