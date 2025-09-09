<script lang="ts">
import { defineComponent, computed } from 'vue';

export default defineComponent({
  name: 'MiniBarComponent',
  props: {
    label: { type: String, required: true },
    value: { type: Number, required: true },
    max:   { type: Number, required: true },
    color: { type: String, default: '' },
  },
  setup(props) {
    const percent = computed(() =>
      Math.min(100, Math.round((props.value / (props.max || 1)) * 100))
    );
    const colorClass = computed(() => props.color ?? '');

    return { percent, colorClass };
  },
});
</script>
<template name="MiniBar">
  <div class="rounded-xl border border-slate-200 bg-white p-4">
    <div class="mb-2 flex items-center justify-between text-sm">
      <span class="text-slate-600">{{ label }}</span>
      <span class="tabular-nums text-slate-900 font-medium">{{ value.toLocaleString() }}</span>
    </div>
    <div class="h-2 w-full overflow-hidden rounded bg-slate-100">
      <div
        class="h-full bg-indigo-500 transition-all"
        :class="colorClass"
        :style="{ width: percent + '%' }"
      />
    </div>
  </div>
</template>
