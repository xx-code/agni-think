<script setup lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { library } from '@fortawesome/fontawesome-svg-core'

library.add(fas)

const props = defineProps(['link', 'icon', 'title'])

const url = useRequestURL()
const isActive =(url.pathname === props.link)

</script>

<template>
    <NuxtLink :href="link" style="text-decoration:none;" >
        <div class="link" :class="{'link-active': isActive}">
            <div>
                <font-awesome-icon style="margin-right:5px" :icon="icon" />
            </div>
            <p>{{ title }}</p>
        </div> 
    </NuxtLink>
</template>

<style scoped lang="scss">
    .link {
        color: #1E3050;
        font-size: 0.90rem;
        border-radius: 15px;
        display: flex;
        align-items: center;
        padding-left: 20px;
        &:hover {
            background: rgba(103, 85, 215, 0.1);
        }
    }
    .link-active {
        background-color: #6755D7;
        color: #fff;
        font-size: 0.90rem;
        border-radius: 15px;
        display: flex;
        align-items: center;
        padding-left: 20px;
        &:hover {
            background: #6755D7; 
        }
    }
</style>