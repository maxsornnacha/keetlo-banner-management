<script lang="ts">
import { defineComponent, onMounted, onUnmounted } from "vue";
import { confirmState } from "@/utils/use-confirm";

export default defineComponent({
  name: "ConfirmComponent",
  setup() {
    const onEsc = (e: KeyboardEvent) => {
      if (confirmState.open.value && e.key === "Escape") {
        confirmState.resolveCancel();
      }
    };

    onMounted(() => window.addEventListener("keydown", onEsc));
    onUnmounted(() => window.removeEventListener("keydown", onEsc));

    return {
      open: confirmState.open,
      text: confirmState.text,
      confirm: confirmState.resolveOk,
      cancel: confirmState.resolveCancel,
    };
  },
});
</script>

<template>
  <teleport to="body">
    <!-- Transition wrapper for opacity + transform -->
    <transition name="fade-slide">
      <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Backdrop (click to cancel) -->
        <div class="absolute inset-0 bg-black/40" @click="cancel"></div>

        <!-- Dialog box -->
        <div class="relative z-10 w-full max-w-sm rounded-lg bg-white shadow-lg ring-1 ring-slate-200">
          <div class="px-4 py-3 border-b border-slate-200 font-semibold">Confirm</div>
          <div class="px-4 py-6 text-slate-700">{{ text }}</div>
          <div class="flex justify-end gap-2 px-4 py-3 border-t border-slate-200">
            <button type="button" class="rounded-md border px-3 py-1.5 text-sm hover:bg-gray-200" @click="cancel">Cancel</button>
            <button type="button" class="rounded-md bg-indigo-600 min-w-[80px] hover:bg-indigo-700 px-3 py-1.5 text-sm text-white" @click="confirm">OK</button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
/* Transition for fade-in and slide effect */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px); /* Adjust the slide amount */
}

.fade-slide-enter-to,
.fade-slide-leave-from {
  opacity: 1;
  transform: translateY(0); /* Final position */
}
</style>
