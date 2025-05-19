<script setup lang="ts">
import { useCurrentRouteInfo } from '../../composables/routing';

const routeInfo = useCurrentRouteInfo(); 

</script>

<template>
    <div class="container-top-bar">
        <div>
            <h1 class="title">{{ routeInfo?.title }}</h1>
            <p class="sub-title">{{ routeInfo?.subtitle }}</p>
        </div>
        <div>
            <TopBarNotificationIcon />
        </div> 
    </div>
</template>

<style scoped lang="scss">
.container-top-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1.05rem;
}
.title {
   color: #000;   
   margin: 0;
}

.sub-title {
    color: #ADADAD;
    margin: 0;
}
</style>