<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { library } from '@fortawesome/fontawesome-svg-core'


library.add(fas)

</script>


<template>
    <div class="container-notification-icon">
        <UChip color="error">
            <font-awesome-icon class="icon" icon="fa-solid fa-bell" />
        </UChip>
    </div>
</template>


<style scoped lang="scss">
.container-notification-icon {
    background: rgba(103, 85, 215, 0.1);
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 45px;
    width: 45px;
}
</style>