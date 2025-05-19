<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { library } from '@fortawesome/fontawesome-svg-core'


library.add(fas)

</script>


<template>
    <div class="container-notification-icon">
        <div style="position: relative;">
            <font-awesome-icon class="icon" icon="fa-solid fa-bell" />
            <div class="alert-btn"/>
        </div>
    </div>
</template>


<style scoped lang="scss">
.container-notification-icon {
    background: rgba(103, 85, 215, 0.1);
    border-radius: 12px;
    padding: 1rem;
}
.alert-btn {
    height: 10px;
    width: 10px;
    border-radius: 100%;
    background-color: #DC4C4C;
    position: absolute;
    bottom: -5px;
    right: -5px;
}
</style>