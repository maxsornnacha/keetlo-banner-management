<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export default defineComponent({
  name: "PaginationComponent",
  props: {
    totalItems: {
      type: Number,
      required: true
    },
    itemsPerPage: {
      type: Number,
      required: true
    },
    currentPage: {
      type: Number,
      required: true
    }
  },
  emits: ['update:currentPage'],
  setup(props) {
    const router = useRouter();
    const route = useRoute();
    const totalPages = computed(() => {
      return Math.ceil(props.totalItems / props.itemsPerPage);
    });

    const nextPage = () => {
      if ((props.currentPage) < totalPages.value) {
        router.push({ query: { ...route.query, page: String(props.currentPage+1) } });
      }
    };

    const prevPage = () => {
      if (props.currentPage > 1) {
       router.push({ query: { ...route.query, page: String(props.currentPage-1) } });
      }
    };

    const goToPage = (page: number) => {
      if (page >= 1 && page <= totalPages.value) {
        router.push({ query: { ...route.query, page: String(page) } });
      }
    };

    const pageNumbers = computed(() => {
      const pages = [];
      for (let i = props.currentPage - 5; i <= props.currentPage+10; i++) {
        if(i > 0 && i < totalPages.value+1){
        pages.push(i);
        }
      }
      return pages;
    });

    return {
      totalPages,
      pageNumbers,
      nextPage,
      prevPage,
      goToPage
    };
  }
});
</script>

<template>
  <div class="pagination-container">
    <div class="pagination-controls flex items-center gap-2 justify-center">
      <!-- Previous Page Button -->
      <button
        @click="prevPage"
        :disabled="currentPage <= 1"
        class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md disabled:opacity-50"
      >
        Prev
      </button>

      <!-- Page Numbers -->
      <div class="page-numbers flex gap-2 p-4 overflow-x-auto">
        <button
          v-for="page in pageNumbers"
          :key="page"
          @click="goToPage(page)"
          :disabled="page === currentPage"
          :class="[
            'px-4 py-2 rounded-md',
            page === currentPage ? 'bg-indigo-500 text-white' : 'bg-white text-gray-700 hover:bg-gray-200'
          ]"
        >
          {{ page }}
        </button>
      </div>

      <!-- Next Page Button -->
      <button
        @click="nextPage"
        :disabled="(currentPage) >= totalPages"
        class="px-4 py-2 bg-gray-200 text-gray-700 rounded-md disabled:opacity-50"
      >
        Next
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination-container {
  padding: 16px;
}

.pagination-controls button {
  transition: background-color 0.2s;
}

.pagination-controls .page-numbers button {
  transition: background-color 0.2s;
}
</style>
