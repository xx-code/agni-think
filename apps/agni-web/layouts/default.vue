<template>
    <div class="container">
        <NavBar />
        <slot />
    </div>
</template>

<style scoped>
.container {
    height: 100vh;
    display: flex;
}
</style>