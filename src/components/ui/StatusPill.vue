<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'StatusPillComponent',
  props: { status: { type: String as () => 'active' | 'inactive' | 'completed', required: true } },
  computed: {
    color(): string {
      return (
        {
          active: 'bg-emerald-100 text-emerald-700 ring-emerald-200',
          inactive: 'bg-slate-100 text-slate-700 ring-slate-200',
          completed: 'bg-indigo-100 text-indigo-700 ring-indigo-200',
        } as Record<string, string>
      )[this.status] || '';
    },
  },
});
</script>
<template name="StatusPillCompoenet">
     <span class="rounded-full px-2.5 py-1 text-xs font-medium ring-1" :class="color">{{ status }}</span>
</template>