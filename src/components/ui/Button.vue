<script lang="ts">
import { defineComponent } from 'vue';
export default defineComponent({
    name: "ButtonComponent"
})
</script>
<template namme="ButtonComponent">
        <button class="px-4 py-2 rounded-lg border">
            <slot></slot>
        </button>
</template>