<script lang="ts">
import Overallloader from "@/components/common/Overallloader.vue";
import Loader from "@/components/ui/Loader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import { useUserStore } from "@/store";
import { Campaign } from "@/types/Campaign";
import { formatDate, formatDateTime } from "@/utils/date-handler";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useAlert } from "@/utils/use-alert";
import { useConfirm } from "@/utils/use-confirm";
import { useSearch } from "@/utils/use-search";
import { storeToRefs } from "pinia";
import { computed, defineComponent, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

export default defineComponent({
  name: "TotalCampaignsPage",
  components: {
    Pagination,
    Overallloader,
    Loader,
  },
  setup() {
    const confirm = useConfirm();
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const route = useRoute();
    const campaigns = ref<Campaign[]>([]);
    const { searchTerm, updateSearch } = useSearch();
    const currentPage = ref(parseInt(route.query.page as string) || 1);
    const totalCampaigns = ref(0);
    const itemsPerPage = ref(10);
    const router = useRouter();
    const { openAlert, closeAlert } = useAlert();
    const errorMessage = ref("");
    const loading = ref(false);
    const loadingDelete = ref(false);
    const fullPath = route.fullPath;
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

     const fmtDate = (
    input: string | number | Date | null | undefined,
    ): string => {
    return formatDate(input, {
      timezone: tz.value,
      dateFormat: df.value,
    });
    }

    const fetchApi = async () => {
      try {
        loading.value = true;
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/campaign?page=${currentPage.value}&limit=${itemsPerPage.value}&search=${searchTerm.value}`
        );
        totalCampaigns.value = responseData.totalCampaigns;
        campaigns.value = responseData.campaigns;
        loading.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loading.value = false;
      }
    };

    onMounted(async () => {
      fetchApi();
    });

    const onSearch = () => {
      updateSearch(searchTerm.value); // update URL
    };

    watch(
      () => ({
        page: route.query.page,
        search: route.query.search,
      }),
      (newQueryParams) => {
        const newPage = parseInt(newQueryParams.page as string) || 1;
        if (newPage !== currentPage.value) {
          currentPage.value = newPage;
        }
        fetchApi();
      }
    );

    const handleViewCampaign = (campaignId: string) => {
      router.push(`/campaigns/${campaignId}?back=${route.fullPath}`);
    };

    const handleDeleteCampaign = async (campaignId: string) => {
      const status = await confirm(
        "Are you sure you are going to delete the campaign"
      );
      if (status) {
        try {
          loadingDelete.value = true;
          await fetchAPI(
            `${process.env.VUE_APP_API_URL}/campaign/delete/${campaignId}`,
            "DELETE"
          );
          loadingDelete.value = false;
          openAlert("The campaign deleted successfully", "success");
          setTimeout(() => {
            window.location.reload();
            closeAlert();
          }, 1500);
        } catch (error: unknown) {
          loadingDelete.value = false;
          openAlert(toErrorMessage(error), "error");
        }
      }
    };

    return {
      imageBaseUrl,
      campaigns,
      currentPage,
      totalCampaigns,
      itemsPerPage,
      handleViewCampaign,
      searchTerm,
      onSearch,
      handleDeleteCampaign,
      fullPath,
      loading,
      loadingDelete,
      errorMessage,
      fmtDateTime,
      fmtDate,
    };
  },
});
</script>

<template>
  <div class="totalCampaigns">
    <div v-if="!errorMessage" class="space-y-16 flex flex-col">
      <div class="space-y-4 bg-white shadow-1 rounded-md">
        <!-- Header -->
        <div class="lg:p-4 py-4">
          <h2 class="text-xl font-semibold text-center lg:text-start mb-0">
            Campaigns
          </h2>
          <div
            class="flex flex-col lg:flex-row justify-between gap-4 items-center"
          >
            <div class="flex gap-2 items-center">
              <h4 class="text-lg font-medium">Total</h4>
              <h4 v-if="loading">
                <Loader customClass="w-7 h-7" />
              </h4>
              <h4 v-else class="font-semibold text-2xl text-indigo-600">
                {{ totalCampaigns }}
              </h4>
              <h4 class="text-lg font-medium">campaigns</h4>
            </div>

            <div class="flex flex-col lg:flex-row gap-2 w-full lg:w-auto">
              <input
                class="p-2 border border-dark-200 rounded-md lg:min-w-[300px]"
                placeholder="Search the name of campaigns"
                v-model="searchTerm"
                @input="onSearch"
              />
              <router-link
                :to="`/campaigns/create?back=${fullPath}&display_project_selection=1`"
              >
                <button
                  class="px-3 py-2 border border-slate-200 rounded-lg hover:bg-slate-100 flex gap-2 items-center"
                >
                  <font-awesome-icon icon="plus" class="w-4 h-4" />
                  <span>Add</span>
                </button>
              </router-link>
            </div>
          </div>
        </div>

        <div v-if="!loading">
          <!-- Table -->
          <div v-if="totalCampaigns > 0">
            <div class="max-w-[100%] overflow-x-auto">
              <table class="text-left">
                <thead>
                  <tr class="bg-slate-100 text-slate-600">
                    <th class="min-w-[200px] px-4 py-3 font-medium w-1/2">
                      Name
                    </th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">Project</th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">
                      Total banners
                    </th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">
                      Start date
                    </th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">
                      End date
                    </th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">
                      Date modified
                    </th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">Status</th>
                    <th class="min-w-[150px] px-4 py-3 font-medium">Actions</th>
                  </tr>
                </thead>

                <tbody>
                  <tr
                    v-for="c in campaigns"
                    :key="c.campaignId"
                    class="border-b hover:bg-slate-50"
                  >
                    <td
                      class="px-4 py-3 cursor-pointer"
                      @click="handleViewCampaign(c.campaignId)"
                    >
                      <div class="flex items-center gap-3">
                        <img
                          :src="
                            c.campaignImageUrl
                              ? imageBaseUrl + c.campaignImageUrl
                              : '/images/total/folder-icon.png'
                          "
                          alt="Project Image"
                          class="object-cover h-10 w-10 rounded-lg"
                        />
                        <div class="flex flex-col">
                          <span class="font-medium">{{ c.campaignName }}</span>
                        </div>
                      </div>
                    </td>

                    <td
                      @click="handleViewCampaign(c.campaignId)"
                      class="px-4 py-3 text-center cursor-pointer"
                    >
                      {{ c.projectName }}
                    </td>

                    <td
                      class="px-4 py-3 text-center cursor-pointer"
                      @click="handleViewCampaign(c.campaignId)"
                    >
                      {{ c.totalBanners }}
                    </td>

                    <td
                      class="px-4 py-3 text-center cursor-pointer"
                      @click="handleViewCampaign(c.campaignId)"
                    >
                      {{ fmtDate(c.startDate) }}
                    </td>
                    <td
                      class="px-4 py-3 text-center cursor-pointer"
                      @click="handleViewCampaign(c.campaignId)"
                    >
                      {{ fmtDate(c.endDate) ? fmtDate(c.endDate) : "Endless" }}
                    </td>

                    <td
                      class="px-4 py-3 cursor-pointer"
                      @click="handleViewCampaign(c.campaignId)"
                    >
                      {{ fmtDateTime(c.updatedAt) }}
                    </td>

                    <td
                      class="px-4 py-3 cursor-pointer"
                      @click="handleViewCampaign(c.campaignId)"
                    >
                      <span
                        :class="[
                          'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium',
                          c.status === 'active'
                            ? 'bg-green-100 text-green-700'
                            : c.status === 'completed'
                            ? 'bg-indigo-200 text-slate-700'
                            : 'bg-slate-200 text-slate-700',
                        ]"
                      >
                        {{ c.status }}
                      </span>
                    </td>

                    <td class="px-4 py-3">
                      <div class="flex items-center gap-2">
                        <router-link
                          :to="`/campaigns/edit/${c.campaignId}?back=${fullPath}`"
                        >
                          <button
                            class="px-2 py-1 text-sm border border-slate-200 rounded-md hover:bg-slate-100"
                            title="View"
                          >
                            Edit
                          </button>
                        </router-link>
                        <button
                          class="px-2 py-1 text-sm border border-slate-200 rounded-md hover:bg-slate-100"
                          title="Edit"
                          @click="handleDeleteCampaign(c.campaignId)"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Pagination -->
            <div class="lg:p-4">
              <Pagination
                :currentPage="currentPage"
                :totalItems="totalCampaigns"
                :itemsPerPage="itemsPerPage"
              />
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="p-8 text-center text-slate-500">
            No campaigns yet. Create your first one!
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="errorMessage"
      class="py-4 my-4 bg-red-50 border border-red-100 rounded-md text-center text-red-600"
    >
      {{ errorMessage }}
    </div>
    <Overallloader :isOpen="loadingDelete" />
  </div>
</template>

<style scoped></style>
