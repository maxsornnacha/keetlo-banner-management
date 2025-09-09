<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'KpiCardComponent',
  props: {
    label: { type: String, required: true },
    value: { type: [Number, String], required: true },
    suffix: { type: String, required: false },
  },
});
</script>
<template name="KpiCardComponent">
    <div class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
      <div class="text-sm text-slate-500">{{ label }}</div>
      <div class="mt-1 text-2xl font-semibold text-slate-800">
        {{ value }}<span v-if="suffix" class="ml-1 text-base font-medium text-slate-500">{{ suffix }}</span>
      </div>
    </div>
</template>