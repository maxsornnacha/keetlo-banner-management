<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'CardComponent',
  props: { class: { type: String, required: false } },
});
</script>
<template name="CardCompoenet">
  <div :class="['rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200', $props.class]">
      <div class="mb-3 flex items-center justify-between">
        <h3 class="text-base font-semibold text-slate-800">
          <slot name="title" />
        </h3>
        <slot name="actions" />
      </div>
      <div>
        <slot name="body" />
      </div>
    </div>
</template>