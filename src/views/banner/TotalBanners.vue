<script lang="ts">
import Loader from "@/components/ui/Loader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import { Banner } from "@/types/Banner";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useSearch } from "@/utils/use-search";
import { defineComponent, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

export default defineComponent({
  name: "TotalBannersPage",
  components: {
    Pagination,
    Loader,
  },
  setup() {
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const route = useRoute();
    const banners = ref<Banner[]>([]);
    const { searchTerm, updateSearch } = useSearch();
    const currentPage = ref(parseInt(route.query.page as string) || 1);
    const totalBanners = ref(0);
    const itemsPerPage = ref(9);
    const fullPath = route.fullPath;
    const router = useRouter();
    const loading = ref(true);
    const errorMessage = ref<string | null>(null);

    const fetchApi = async () => {
      try {
        loading.value = true;
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/banner?page=${currentPage.value}&limit=${itemsPerPage.value}&search=${searchTerm.value}`
        );
        totalBanners.value = responseData.totalBanners;
        banners.value = responseData.banners;
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

    const handleViewBanner = (bannerId: string) => {
      router.push(`/banners/${bannerId}?back=${route.fullPath}`);
    };

    return {
      imageBaseUrl,
      banners,
      currentPage,
      totalBanners,
      itemsPerPage,
      handleViewBanner,
      searchTerm,
      onSearch,
      fullPath,
      loading,
      errorMessage,
    };
  },
});
</script>

<template>
  <div class="totalBanners">
    <div v-if="!errorMessage" class="space-y-16 flex flex-col">
      <div class="space-y-4 bg-white shadow-1 rounded-md">
        <!-- Header -->
        <div class="lg:p-4 py-4">
          <h2 class="text-xl font-semibold text-center lg:text-start mb-0">
            banners
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
                {{ totalBanners }}
              </h4>
              <h4 class="text-lg font-medium">banners</h4>
            </div>

            <div class="flex flex-col lg:flex-row gap-2 w-full lg:w-auto">
              <input
                class="p-2 border border-dark-200 rounded-md lg:min-w-[300px]"
                placeholder="Search the name of banners"
                v-model="searchTerm"
                @input="onSearch"
              />
              <router-link
                :to="`/banners/create?back=${fullPath}&display_campaign_selection=1`"
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
          <!-- Banners card -->
          <div v-if="totalBanners > 0">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <div
                :key="banner.bannerId"
                v-for="banner in banners"
                class="border border-slate-200 hover:border-none rounded-md shadow-md hover:shadow-lg"
              >
                <div
                  class="w-full h-[200px] bg-slate-200 overflow-hidden flex justify-center items-center"
                >
                  <div
                    @click="handleViewBanner(banner.bannerId)"
                    class="hover:scale-110 transform transition-all duration-300 cursor-pointer rounded bg-slate-50 flex items-center justify-center overflow-hidden"
                    style="max-width: 300px; max-height: 200px"
                  >
                    <img
                      :src="`${imageBaseUrl}${banner.imageUrl}`"
                      alt=""
                      :class="`w-[${banner.imageWidth}px] h-[${banner.imageHeight}px]`"
                      :style="{
                        aspectRatio:
                          banner.imageWidth && banner.imageHeight
                            ? `${banner.imageWidth} / ${banner.imageHeight}`
                            : 'auto',
                      }"
                    />
                  </div>
                </div>
                <div class="p-4">
                  <div
                    cursor-pointer
                    class="pb-4 border-b cursor-pointer hover:text-indigo-600"
                    @click="handleViewBanner(banner.bannerId)"
                  >
                    <p class="text-base font-semibold">
                      {{ banner.bannerName }}
                    </p>
                    <p class="line-clamp-3 text-slate-500">
                      {{
                        banner.description
                          ? banner.description
                          : "No description"
                      }}
                    </p>
                  </div>
                  <div class="py-4 border-t">
                    <p>
                      <span class="font-semibold">Project:</span>
                      {{ banner.projectName }}
                    </p>
                    <p>
                      <span class="font-semibold">Campaign:</span>
                      {{ banner.campaignName }}
                    </p>
                  </div>
                  <div class="pt-4 border-t">
                    <p>
                      Size: {{ banner.imageWidth ?? "?" }}×{{
                        banner.imageHeight ?? "?"
                      }}
                      px
                    </p>
                    <p>Views: {{ banner.views ?? 0 }}</p>
                    <p>Clicks: {{ banner.clicks ?? 0 }}</p>
                    <p>
                      Link:
                      <a
                        v-if="banner.link"
                        :href="banner.link"
                        target="_blank"
                        class="text-indigo-600 hover:underline"
                        >open</a
                      >
                      <span v-else>None</span>
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Pagination -->
            <div class="lg:p-4">
              <Pagination
                :currentPage="currentPage"
                :totalItems="totalBanners"
                :itemsPerPage="itemsPerPage"
              />
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="p-8 text-center text-slate-500">
            No banners yet. Create your first one!
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
  </div>
</template>

<style scoped></style>
