<script lang="ts">
import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "AlertComponent",
  setup() {
    const isOpen = ref(false);
    const message = ref("");
    const type = ref<"success" | "error" | "warn" | "info">("info");

    const openAlert = (msg: string, alertType: "success" | "error" | "warn" | "info" = "info") => {
      message.value = msg;
      type.value = alertType;
      isOpen.value = true;
    };

    const closeAlert = () => {
      isOpen.value = false;
    };

    return {
      isOpen,
      message,
      closeAlert,
      openAlert,
      type
    };
  },
});
</script>

<template>
  <teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/40" @click="closeAlert"></div>

      <!-- Dialog box -->
      <div
        class="relative z-10 w-full max-w-sm rounded-lg p-4 shadow-lg ring-1 ring-slate-200"
        :class="`bg-white`"
      >
        <div class="flex justify-between items-center border-b pb-2">
          <div class="font-semibold">{{type.toUpperCase()}}</div>
          <button
            @click="closeAlert"
            class="text-lg font-semibold text-slate-700 hover:text-slate-900"
            aria-label="Close alert"
          >
            ✕
          </button>
        </div>
        <div class="mt-2">
          <div v-if="type === 'success' || type === 'error' || type === 'warn'" class="flex justify-center py-4">
              <font-awesome-icon v-if="type === 'success'" icon="check" class="bg-emerald-400 w-9 h-9 p-2 rounded-full text-white"/>
              <font-awesome-icon v-if="type === 'error'" icon="xmark" class="bg-red-400 w-9 h-9 p-2 rounded-full text-white"/>
              <font-awesome-icon v-if="type === 'warn'" icon="triangle-exclamation" class="bg-yellow-400 w-9 h-9 p-2 rounded-full text-white"/>
          </div>
          <p class="text-sm text-center">{{ message }}</p>
        </div>
      </div>
    </div>
  </teleport>
</template>

<style scoped>
/* Transition for fade-in and fade-out effect */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.fade-enter-to,
.fade-leave-from {
  opacity: 1;
}
</style>
