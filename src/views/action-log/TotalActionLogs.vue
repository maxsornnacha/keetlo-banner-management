<script lang="ts">
import { defineComponent, ref, onMounted, watch, computed } from "vue";
import { fetchAPI } from "@/utils/fetch-api"; // assuming fetchAPI is a utility to fetch data
import { ActionLog } from "@/types/ActionLog";
import { useSearch } from "@/utils/use-search";
import { useRoute, useRouter } from "vue-router";
import Pagination from "@/components/ui/Pagination.vue";
import { useUserStore } from "@/store";
import { storeToRefs } from "pinia";
import { formatDateTime } from "@/utils/date-handler";

export default defineComponent({
  name: "ActionLogsPage",
  components: {
    Pagination
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const actionLogs = ref<ActionLog[]>([]);
    const type = ref(route.query.type || "");
    const { searchTerm, updateSearch } = useSearch();
    const currentPage = ref(parseInt(route.query.page as string) || 1);
    const totalActionLogs = ref(0);
    const itemsPerPage = ref(20);
    const errorMessage = ref<string | null>(null);
    const loading = ref(true);
    const userStore = useUserStore()
    const { user } = storeToRefs(userStore);
    const tz = computed(() => user.value?.timezone ?? 'UTC');
    const df = computed(() => user.value?.dateFormat ?? 'YYYY-MM-DD');

    const fmtDateTime = (
    input: string | number | Date | null | undefined,
    ): string => {
    return formatDateTime(input, {
      timezone: tz.value,
      dateFormat: df.value,
    });
    }

    const fetchLogs = async () => {
      try {
        loading.value = true;
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/action-logs?page=${currentPage.value}&limit=${itemsPerPage.value}&type=${type.value}&search=${searchTerm.value}`
        );
        actionLogs.value = responseData.actionLogs || [];
        totalActionLogs.value = responseData.totalActionLogs || 0;
        loading.value = false;
      } catch (error) {
        errorMessage.value = "Failed to fetch action logs:" + error;
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchLogs();
    });

    watch(type, (value)=>{
       const newQuery = { ...route.query, type: value || undefined, page: 1 }; // remove if empty
        router.push({ query: newQuery });
    })

    watch(
      () => ({
        page: route.query.page,
        search: route.query.search,
        type: route.query.type,
      }),
      (newQueryParams) => {
        const newPage = parseInt(newQueryParams.page as string) || 1;
        if (newPage !== currentPage.value) {
          currentPage.value = newPage;
        }
        fetchLogs();
      }
    );

    const onSearch = () => {
      updateSearch(searchTerm.value); // update URL
    };

    return {
      actionLogs,
      type,
      errorMessage,
      searchTerm,
      onSearch,
      currentPage,
      itemsPerPage,
      totalActionLogs,
      loading,
      fmtDateTime,
    };
  },
});
</script>
<template>
  <div class="action-logs-container py-4">
    <header class="mb-6">
      <h1 class="text-2xl font-semibold text-slate-800">Action Logs</h1>
      <p class="text-sm text-slate-500">
        View and manage logs for actions taken across the system.
      </p>
    </header>

    <!-- Search Filters -->
    <div class="mb-4">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div>
          <label for="type" class="block text-sm text-slate-600">Type</label>
          <select v-model="type" id="type" class="input">
            <option value="">All Types</option>
            <option value="project">Project</option>
            <option value="campaign">Campaign</option>
            <option value="banner">Banner</option>
            <option value="user">User</option>
            <option value="auth">Auth</option>
            <option value="setting">Setting</option>
          </select>
        </div>
        <div>
          <label for="topic" class="block text-sm text-slate-600">Topic</label>
          <input
            v-model="searchTerm"
            @input="onSearch"
            id="search"
            type="text"
            class="input"
            placeholder="Enter Topic"
          />
        </div>
      </div>
    </div>

    <div v-if="!loading">
    <!-- Logs Table -->
    <div v-if="actionLogs.length > 0">
      <div class="overflow-x-auto">
      <table class="min-w-full bg-white rounded-lg shadow-md">
        <thead>
          <tr>
            <th
              class="py-2 px-4 text-left text-sm font-semibold text-slate-600"
            >
              ID
            </th>
            <th
              class="py-2 px-4 text-left text-sm font-semibold text-slate-600"
            >
              Type
            </th>
            <th
              class="py-2 px-4 text-left text-sm font-semibold text-slate-600"
            >
              Topic
            </th>
            <th
              class="py-2 px-4 text-left text-sm font-semibold text-slate-600"
            >
              Message
            </th>
            <th
              class="py-2 px-4 text-left text-sm font-semibold text-slate-600"
            >
              Created At
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(log, index) in actionLogs" :key="index">
            <td class="py-2 px-4">{{ ((currentPage - 1)*itemsPerPage) + index + 1 }}</td>
            <td class="py-2 px-4">{{ log.type }}</td>
            <td class="py-2 px-4">{{ log.topic }}</td>
            <td class="py-2 px-4">{{ log.message }}</td>
            <td class="py-2 px-4">{{ fmtDateTime(log.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      </div>

         <div class="lg:p-4">
            <Pagination
              :currentPage="currentPage"
              :totalItems="totalActionLogs"
              :itemsPerPage="itemsPerPage"
            />
          </div>
    </div>
    
    <div v-if="actionLogs.length === 0">
      <p class="text-center text-sm text-slate-500">
        No action logs found for the given filters.
      </p>
    </div>
    </div>
        <div v-if="errorMessage">
      <p class="text-center text-sm text-slate-500">{{ errorMessage }}</p>
    </div>
  </div>
</template>
<style scoped>
/* Styling for input fields and table */
.input {
  @apply w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 disabled:bg-slate-100;
}

table {
  @apply w-full table-auto border-collapse;
}

th,
td {
  @apply py-2 px-4 text-sm font-normal text-slate-600;
  border-bottom: 1px solid #e2e8f0;
}

th {
  @apply text-slate-800;
}

tr:hover {
  @apply bg-slate-50;
}
</style>
