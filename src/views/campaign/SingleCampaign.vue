<!-- src/views/SingleCampaignPage.vue -->
<script lang="ts">
import Overallloader from "@/components/common/Overallloader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import { useUserStore } from "@/store";
import { Banner } from "@/types/Banner";
import { Campaign } from "@/types/Campaign";
import { formatDate, formatDateTime } from "@/utils/date-handler";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useAlert } from "@/utils/use-alert";
import { useConfirm } from "@/utils/use-confirm";
import { useSearch } from "@/utils/use-search";
import { storeToRefs } from "pinia";
import { defineComponent, ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

export default defineComponent({
  name: "SingleCampaignPage",
  components: {
    Pagination,
    Overallloader,
  },
  setup() {
    const confirm = useConfirm();
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const route = useRoute();
    const router = useRouter();
    const slug = route.params.slug as string;
    const back = route.query.back || "/campaigns";
    const campaign = ref<Campaign | null>(null);
    const banners = ref<Banner[]>([]);
    const { searchTerm, updateSearch } = useSearch();
    const currentPage = ref(parseInt(route.query.page as string) || 1);
    const totalBanners = ref(0);
    const itemsPerPage = ref(10);
    const currentPath = route.path;
    const { openAlert, closeAlert } = useAlert();
    const loading = ref(true);
    const errorMessage = ref<string | null>(null);
    const loadingDelete = ref(false);

    const imagePreview = ref<string | null>(null);

    const statusClass = computed(() => {
      const status = campaign?.value?.status;
      if (status === "active")
        return "bg-emerald-100 text-emerald-700 ring-emerald-200";
      if (status === "completed")
        return "bg-indigo-100 text-indigo-700 ring-indigo-200";
      return "bg-slate-100 text-slate-700 ring-slate-200";
    });

    const dateRange = computed(() => {
      const s = campaign?.value?.startDate;
      const e = campaign?.value?.endDate;
      return `${fmtDate(s)} - ${fmtDate(e) ? fmtDate(e) : "Endless"}`;
    });

    const hasBanners = computed(() => (totalBanners.value ?? 0) > 0);
    const backPath = computed<string>(() => {
      const q = route.query.back;
      const back = Array.isArray(q) ? q[0] : q;
      return back && back.trim() ? back : "/projects";
    });

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

    const goCreateBanner = () => {
      router.push(`/banners/create?back=${route.fullPath}&campaign_id=${slug}`);
    };

    const goEditCampaign = () => {
      router.push(`/campaigns/edit/${slug}`);
    };

    const fetchApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/campaign/${slug}?page=${currentPage.value}&limit=${itemsPerPage.value}&search=${searchTerm.value}`
        );
        totalBanners.value = responseData.totalBanners || 0;
        campaign.value = responseData.campaign;
        banners.value = responseData.banners;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
      } finally {
        loading.value = false;
      }
    };

    onMounted(async () => {
      fetchApi();
    });

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

    const handleViewBanner = (bannerId: string) => {
      router.push(`/banners/${bannerId}?back=${route.fullPath}`);
    };

    const handleDeleteCampaign = async (
      campaignId: string | null | undefined
    ) => {
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
            router.push(back as string);
            closeAlert();
          }, 1500);
        } catch (error: unknown) {
          openAlert(toErrorMessage(error), "error");
          loadingDelete.value = false;
        }
      }
    };

    return {
      slug,
      loading,
      errorMessage,
      campaign,
      totalBanners,
      itemsPerPage,
      banners,
      currentPage,
      updateSearch,
      currentPath,
      statusClass,
      dateRange,
      hasBanners,
      imagePreview,
      goCreateBanner,
      goEditCampaign,
      imageBaseUrl,
      backPath,
      handleDeleteCampaign,
      handleViewBanner,
      loadingDelete,
      fmtDateTime,
      fmtDate,
    };
  },
});
</script>

<template>
  <div v-if="!loading && !errorMessage" class="py-4">
    <!-- Header -->
    <header class="mb-6 flex items-center justify-between">
      <div class="flex flex-col justify-center gap-4 w-full">
        <!-- Back -->
        <div class="mb-4">
          <router-link :to="backPath">
            <button class="flex items-center gap-1 hover:text-indigo-700">
              <font-awesome-icon icon="chevron-left" class="w-7 h-7" />
              <p class="text-base">Back</p>
            </button>
          </router-link>
        </div>
        <div class="flex flex-col lg:flex-row justify-between gap-4">
          <h1 class="truncate text-2xl font-semibold text-slate-800">
            {{ campaign?.campaignName || "" }}
          </h1>
          <div class="flex items-center gap-2">
            <button
              class="rounded-lg border border-slate-300 px-6 py-2 text-sm hover:bg-gray-200"
              @click="goEditCampaign"
            >
              Edit Campaign
            </button>
            <button
              class="px-6 py-2 text-sm bg-indigo-500 text-white hover:bg-indigo-700 rounded-md"
              title="Edit"
              @click="handleDeleteCampaign(campaign?.campaignId)"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- Loading / errorMessage -->
    <div
      v-if="loading"
      class="rounded-xl border border-slate-200 bg-white p-6 text-slate-600"
    >
      Loading campaign…
    </div>
    <div
      v-else-if="errorMessage"
      class="rounded-xl border border-rose-200 bg-white p-6 text-rose-600"
    >
      {{ errorMessage }}
    </div>

    <!-- Content -->
    <div v-else class="space-y-6">
      <!-- Summary Card -->
      <section class="grid gap-4 lg:grid-cols-3">
        <div
          class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200 lg:col-span-2"
        >
          <div
            class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between"
          >
            <div class="min-w-0">
              <div class="mb-1 text-sm text-slate-500">
                Campaign ID: {{ campaign?.campaignId }}
              </div>
              <div class="text-sm text-slate-500">
                Project ID: {{ campaign?.projectId ?? "None" }}
              </div>
            </div>
            <span
              class="min-w-14 max-w-[70px] lg:max-w-auto inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium flex justify-center ring-1"
              :class="statusClass"
            >
              {{ campaign?.status }}
            </span>
          </div>

          <div class="mt-4 grid gap-4 md:grid-cols-3">
            <div>
              <div class="text-sm text-slate-500">Date Range</div>
              <div class="font-medium text-slate-800">{{ dateRange }}</div>
            </div>
            <div>
              <div class="text-sm text-slate-500">Total Banners</div>
              <div class="font-medium text-slate-800">
                {{ totalBanners ?? 0 }}
              </div>
            </div>
            <div>
              <div class="text-sm text-slate-500">Updated At</div>
              <div class="font-medium text-slate-800">
                {{ fmtDateTime(campaign?.updatedAt) }}
              </div>
            </div>
          </div>

          <div class="mt-4">
            <div class="text-sm text-slate-500">Description</div>
            <p class="mt-1 whitespace-pre-wrap text-slate-800">
              {{ campaign?.description || "No description" }}
            </p>
          </div>
        </div>

        <!-- Optional: campaign image -->
        <div class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
          <div class="text-sm text-slate-500">Campaign Image</div>
          <div class="mt-2 flex justify-center">
            <img
              :src="
                imagePreview || campaign?.campaignImageUrl
                  ? `${imageBaseUrl}${campaign?.campaignImageUrl}`
                  : '/images/total/folder-icon.png'
              "
              alt="campaign"
              class="h-40 w-40 rounded-lg object-cover"
            />
          </div>
        </div>
      </section>

      <!-- Banners -->
      <section class="rounded-2xl bg-white p-4 shadow-sm ring-1 ring-slate-200">
        <div class="mb-8 flex items-center justify-between">
          <h3 class="text-base font-semibold text-slate-800">Banners</h3>
          <button
            class="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm text-white hover:bg-indigo-500"
            @click="goCreateBanner"
          >
            + Create Banner
          </button>
        </div>

        <!-- Empty state -->
        <div
          v-if="!hasBanners"
          class="flex items-center justify-between rounded-lg border border-dashed border-slate-300 p-4 my-8"
        >
          <div>
            <div class="font-medium text-slate-800">No banners yet</div>
            <div class="text-sm text-slate-500">
              Create your first banner for this campaign.
            </div>
          </div>
        </div>

        <!-- Table -->
        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="text-left text-slate-500">
                <th class="py-2 px-3 text-center">Banner</th>
                <th class="py-2 px-3 text-center">Details</th>
                <th class="py-2 px-3 text-center">Updated</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="b in banners"
                :key="b.bannerId"
                class="border-t hover:bg-slate-50"
              >
                <td
                  class="py-2 px-3 border cursor-pointer"
                  @click="handleViewBanner(b.bannerId)"
                >
                  <div class="flex items-center gap-3">
                    <div
                      class="rounded ring-1 ring-slate-200 bg-slate-50 flex items-center justify-center overflow-hidden"
                      style="max-width: 300px; max-height: 200px"
                    >
                      <img
                        :src="`${imageBaseUrl}${b.imageUrl}`"
                        alt=""
                        class="object-contain w-full h-full"
                        :style="{
                          aspectRatio:
                            b.imageWidth && b.imageHeight
                              ? `${b.imageWidth} / ${b.imageHeight}`
                              : 'auto',
                        }"
                      />
                    </div>
                  </div>
                </td>
                <td
                  class="py-2 px-3 border text-end cursor-pointer"
                  @click="handleViewBanner(b.bannerId)"
                >
                  <p>Name: {{ b.bannerName }}</p>
                  <p>
                    Size: {{ b.imageWidth ?? "?" }}×{{ b.imageHeight ?? "?" }}
                    px
                  </p>
                  <p>Views: {{ b.views ?? 0 }}</p>
                  <p>Clicks: {{ b.clicks ?? 0 }}</p>
                  <p>
                    Link:
                    <a
                      v-if="b.link"
                      :href="b.link"
                      target="_blank"
                      class="text-indigo-600 hover:underline"
                      >open</a
                    >
                    <span v-else>None</span>
                  </p>
                </td>
                <td
                  class="py-2 px-3 border text-end cursor-pointer"
                  @click="handleViewBanner(b.bannerId)"
                >
                  {{
                    fmtDateTime(b.updatedAt)
                  }}
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Pagination -->
          <div>
            <Pagination
              :currentPage="currentPage"
              :totalItems="totalBanners"
              :itemsPerPage="itemsPerPage"
            />
          </div>
        </div>
      </section>
    </div>
  </div>
  <div
    v-if="errorMessage"
    class="py-4 my-4 bg-red-50 border border-red-100 rounded-md text-center text-red-600"
  >
    {{ errorMessage }}
  </div>
  <Overallloader :isOpen="loadingDelete" />
</template>
