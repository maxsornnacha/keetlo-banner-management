<script lang="ts">
import { defineComponent } from 'vue';
import Loader from '../ui/Loader.vue';


export default defineComponent({
    name: "OverallLoader",
    components: {
        Loader,
    },
    props: {
       isOpen: {
            type: Boolean,
            default: false,
       } 
    }
})
</script>
<template>
    <teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/40"></div>
      <!-- Dialog box -->
      <div
        class="relative z-10 w-full flex justify-center"
      >
        <Loader customClass="w-[60px] h-[60px]"/>
      </div>
    </div>
  </teleport>
</template>