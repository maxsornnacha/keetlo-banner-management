<script lang="ts">
import { defineComponent, onMounted, onUnmounted, PropType, watch, ref, nextTick } from "vue";

type Side = "left" | "right" | "top" | "bottom";

export default defineComponent({
  name: "DrawerComponent",
  props: {
    topic: {type: String},
    modelValue: { type: Boolean, required: true },
    side: { type: String as PropType<Side>, default: "right" },
    width: { type: String, default: "24rem" },
    height: { type: String, default: "75vh" },       
    closeOnEsc: { type: Boolean, default: true },
    closeOnBackdrop: { type: Boolean, default: true },
    zIndex: { type: [Number, String], default: 50 },
    ariaLabel: { type: String, default: "Drawer panel" },
  },
  emits: ["update:modelValue", "open", "close"],
  setup(props, { emit }) {
    const panelRef = ref<HTMLElement | null>(null);
    let previousBodyOverflow: string | null = null;

    const lockBody = () => {
      previousBodyOverflow = document.body.style.overflow || "";
      document.body.style.overflow = "hidden";
    };
    const unlockBody = () => {
      document.body.style.overflow = previousBodyOverflow ?? "";
    };

    const close = () => emit("update:modelValue", false);

    const onKeydown = (e: KeyboardEvent) => {
      if (!props.modelValue) return;
      if (props.closeOnEsc && e.key === "Escape") {
        e.stopPropagation();
        close();
      }
    };

    const focusPanel = async () => {
      await nextTick();
      panelRef.value?.focus();
    };

    watch(
      () => props.modelValue,
      (open) => {
        if (open) {
          lockBody();
          focusPanel();
          emit("open");
        } else {
          unlockBody();
          emit("close");
        }
      },
      { immediate: true }
    );

    onMounted(() => window.addEventListener("keydown", onKeydown));
    onUnmounted(() => {
      window.removeEventListener("keydown", onKeydown);
      unlockBody();
    });

    const onKeydownTrap = (e: KeyboardEvent) => {
      if (e.key !== "Tab") return;
      const root = panelRef.value;
      if (!root) return;
      const focusable = root.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), textarea, input, select, [tabindex]:not([tabindex="-1"])'
      );
      if (!focusable.length) return;

      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        last.focus();
        e.preventDefault();
      } else if (!e.shiftKey && document.activeElement === last) {
        first.focus();
        e.preventDefault();
      }
    };

    const panelStyle = () => {
      const base: Record<string, string> = {};
      if (props.side === "left" || props.side === "right") base.maxWidth = props.width;
      if (props.side === "top" || props.side === "bottom") base.maxheight = props.height;
      return base;
    };

    const sideClasses = () => {
      switch (props.side) {
        case "left":
          return { base: "left-0 top-0 h-full", enter: "-translate-x-full", exit: "translate-x-0" };
        case "right":
          return { base: "right-0 top-0 h-full", enter: "translate-x-full", exit: "translate-x-0" };
        case "top":
          return { base: "top-0 left-0 w-full", enter: "-translate-y-full", exit: "translate-y-0" };
        case "bottom":
          return { base: "bottom-0 left-0 w-full", enter: "translate-y-full", exit: "translate-y-0" };
      }
    };

    return {
      panelRef,
      close,
      onKeydownTrap,
      panelStyle,
      sideClasses,
      topicProp: props.topic,
    };
  },
});
</script>

<template>
  <teleport to="body">
    <transition name="fade">
      <div
        v-if="modelValue"
        class="fixed inset-0 bg-black/40"
        :style="{ zIndex: String(zIndex) }"
        @click="closeOnBackdrop ? close() : null"
        aria-hidden="true"
      />
    </transition>

    <transition
      :name="`slide-${side}`"
    >
      <div
        v-if="modelValue"
        class="fixed bg-white shadow-xl ring-1 ring-slate-200 outline-none"
        :class="[
          'focus-visible:ring-2 focus-visible:ring-indigo-500',
          sideClasses().base,
          'transform transition-transform duration-300 will-change-transform'
        ]"
        :style="{
          ...panelStyle(),
          zIndex: String(Number(zIndex) + 1)
        }"
        role="dialog"
        :aria-label="ariaLabel"
        aria-modal="true"
        tabindex="-1"
        ref="panelRef"
        @keydown="onKeydownTrap"
      >
        <div class="flex items-center justify-between border-b border-slate-200 px-4 py-3">
          <slot name="title">
            <h2 class="text-base font-semibold text-slate-800">{{topicProp ? topicProp :"Drawer"}}</h2>
          </slot>
          <button
            class="inline-flex items-center rounded-md px-2 py-1 text-slate-600 hover:bg-slate-100"
            type="button"
            aria-label="Close"
            @click="close"
          >
            ✕
          </button>
        </div>

        <div class="overflow-auto max-h-[calc(100vh-56px)]">
          <slot />
        </div>

        <div class="border-t border-slate-200 px-4 py-3">
          <slot name="footer" />
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-left-enter-from  { transform: translateX(-100%); }
.slide-left-leave-to    { transform: translateX(-100%); }

.slide-right-enter-from { transform: translateX(100%); }
.slide-right-leave-to   { transform: translateX(100%); }

.slide-top-enter-from   { transform: translateY(-100%); }
.slide-top-leave-to     { transform: translateY(-100%); }

.slide-bottom-enter-from{ transform: translateY(100%); }
.slide-bottom-leave-to  { transform: translateY(100%); }
</style>
