<!-- src/components/AutoCompleteComponent.vue -->
<script lang="ts">
import { defineComponent, PropType, ref, computed, watch } from "vue";

export interface Option {
  value: string;
  label: string;
}

export default defineComponent({
  name: "AutoCompleteComponent",
  props: {
    options: {
      type: Array as PropType<Option[]>,
      required: true,
      default: () => [],
    },
    modelValue: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "Search...",
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const searchText = ref("");
    const open = ref(false);

    const filteredOptions = computed(() => {
      const q = searchText.value.trim().toLowerCase();
      if (!q) return props.options;
      return props.options.filter((o) =>
        o.label.toLowerCase().includes(q)
      );
    });

    watch(
      () => props.modelValue,
      (val) => {
        const match = props.options.find((o) => o.value === val);
        searchText.value = match ? match.label : "";
      },
      { immediate: true }
    );

    const selectOption = (opt: Option) => {
      emit("update:modelValue", opt.value);
      searchText.value = opt.label;
      open.value = false;
    };

    const onInput = () => {
      open.value = true;
    };

    const onBlur = () => {
      // optional: close after a tick so click can register
      setTimeout(() => (open.value = false), 100);
    };

    return {
      searchText,
      filteredOptions,
      selectOption,
      onInput,
      onBlur,
      open,
      placeholderProp: props.placeholder,
    };
  },
});
</script>

<template>
  <div class="relative w-full">
    <input
      v-model="searchText"
      type="text"
      :placeholder="placeholderProp"
      class="w-full p-2 border border-gray-300 rounded-md"
      @input="onInput"
      @focus="open = true"
      @blur="onBlur"
      autocomplete="off"
    />

    <ul
      v-if="open && filteredOptions.length"
      class="absolute z-10 mt-1 max-h-40 w-full overflow-auto rounded-md border border-gray-200 bg-white shadow-lg"
    >
      <li
        v-for="opt in filteredOptions"
        :key="opt.value"
        @mousedown.prevent="selectOption(opt)"
        class="cursor-pointer px-3 py-2 hover:bg-indigo-100"
      >
        {{ opt.label }}
      </li>
    </ul>
  </div>
</template>
