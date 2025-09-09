<script lang="ts">
import Overallloader from "@/components/common/Overallloader.vue";
import Loader from "@/components/ui/Loader.vue";
import { Banner } from "@/types/Banner";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { isFile } from "@/utils/image-check";
import { fileToBase64 } from "@/utils/image-handler";
import { useAlert } from "@/utils/use-alert";
import { useConfirm } from "@/utils/use-confirm";
import { defineComponent, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";

export default defineComponent({
  name: "SingleBannerPage",
  components: {
    Overallloader,
    Loader,
  },
  setup() {
    type Form = {
      bannerName: string;
      description: string;
      imageUrl: null | File;
      imageUrlPreview: null | string;
      imageWidth: number;
      imageHeight: number;
      link: string;
    };
    const confirm = useConfirm();
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const route = useRoute();
    const router = useRouter();
    const slug = route.params.slug as string;
    const back = route.query.back || "/banners";
    const tab = ref(route.query.tab || "banner-information");
    const banner = ref<Banner | null>(null);
    const original = ref<Form>({
      bannerName: "",
      description: "",
      imageUrl: null,
      imageUrlPreview: null,
      imageWidth: 0,
      imageHeight: 0,
      link: "",
    });
    const form = ref<Form>({
      bannerName: "",
      description: "",
      imageUrl: null,
      imageUrlPreview: null,
      imageWidth: 0,
      imageHeight: 0,
      link: "",
    });
    const currentPath = route.fullPath;
    const loading = ref(true);
    const errorMessage = ref<string | null>(null);
    const loadingEditSubmit = ref(false);
    const errorEditMessage = ref<string | null>(null);
    const loadingDelete = ref(false);
    const { openAlert, closeAlert } = useAlert();

    const fetchApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/banner/${slug}`
        );
        banner.value = responseData.banner || 0;
        const base = {
          bannerName: responseData?.banner?.bannerName || "",
          description: responseData?.banner?.description || "",
          imageUrl: responseData?.banner?.imageUrl || "",
          imageUrlPreview: responseData?.banner?.imageUrl || null,
          imageWidth: responseData?.banner?.imageWidth || 0,
          imageHeight: responseData?.banner?.imageHeight || 0,
          link: responseData?.banner?.link || "",
        };
        form.value = { ...base };
        original.value = { ...base };
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
      } finally {
        loading.value = false;
      }
    };

    onMounted(async () => {
      fetchApi();
    });

    const handleChangeTab = (value: string) => {
      tab.value = value;
      const newQuery = { ...route.query, tab: value || undefined }; // remove if empty
      router.push({ query: newQuery });
      return;
    };

    const onPickImage = (e: Event) => {
      const file = (e.target as HTMLInputElement).files?.[0] || null;
      form.value.imageUrl = file;
      form.value.imageUrlPreview = null;
      form.value.imageWidth = 0;
      form.value.imageHeight = 0;

      if (!file) return;
      const objectUrl = URL.createObjectURL(file);
      form.value.imageUrlPreview = objectUrl;

      const img = new Image();
      img.onload = () => {
        form.value.imageWidth = img.naturalWidth || 0;
        form.value.imageHeight = img.naturalHeight || 0;
        URL.revokeObjectURL(objectUrl);
      };
      img.onerror = () => URL.revokeObjectURL(objectUrl);
      img.src = objectUrl;
    };

    const validate = (): string | null => {
      if (!form.value.imageUrl) return "Please select a banner image.";
      if (!form.value.bannerName) return "Please select a banner name.";
      if (!form.value.imageWidth) return "Please select a banner image width.";
      if (!form.value.imageHeight)
        return "Please select a banner image height.";
      if (form.value.link && !/^https?:\/\//i.test(form.value.link)) {
        return "Link must start with http:// or https://";
      }
      return null;
    };

    const onValidate = () => {
      if (!form.value.imageUrl) return false;
      if (!form.value.bannerName) return false;
      if (!form.value.imageWidth) return false;
      if (!form.value.imageHeight) return false;
      return true;
    };

    const submitUpdateBanner = async () => {
      errorEditMessage.value = null;
      const v = validate();
      if (v) {
        errorEditMessage.value = v;
        return;
      }

      try {
        const status = await confirm(
          "Would you like to update the banner information ?"
        );
        if (status) {
          loadingEditSubmit.value = true;

          let base64Image: string | null = null;
          if (form.value.imageUrl && isFile(form.value.imageUrl)) {
            base64Image = await fileToBase64(form.value.imageUrl);
          }

          const payload = {
            bannerName: form.value.bannerName,
            description: form.value.description,
            imageUrl: base64Image ? base64Image : form.value.imageUrl,
            imageWidth: form.value.imageWidth,
            imageHeight: form.value.imageHeight,
            link: form.value.link ? form.value.link : null,
          };
          await fetchAPI(
            `${process.env.VUE_APP_API_URL}/banner/update/${slug}`,
            "PUT",
            {},
            payload
          );
          openAlert("Banner updated successfully!", "success");
          setTimeout(() => {
            closeAlert();
            window.location.reload();
          }, 1500);
        }
      } catch (error: unknown) {
        errorEditMessage.value = toErrorMessage(error);
      } finally {
        loadingEditSubmit.value = false;
      }
    };

    const handleDeleteBanner = async (bannerId: string | null | undefined) => {
      const status = await confirm(
        "Are you sure you are going to delete the banner"
      );
      if (status) {
        try {
          loadingDelete.value = true;
          await fetchAPI(
            `${process.env.VUE_APP_API_URL}/banner/delete/${bannerId}`,
            "DELETE"
          );
          loadingDelete.value = false;
          openAlert("The banner deleted successfully", "success");
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

    const dataHasChanged = () => {
      return !(
        original.value.bannerName === form.value.bannerName &&
        original.value.description === form.value.description &&
        original.value.imageUrl === form.value.imageUrl &&
        original.value.imageUrlPreview === form.value.imageUrlPreview &&
        original.value.imageWidth === form.value.imageWidth &&
        original.value.imageHeight === form.value.imageHeight &&
        original.value.link === form.value.link
      );
    };

    return {
      banner,
      imageBaseUrl,
      back,
      tab,
      handleChangeTab,
      currentPath,
      submitUpdateBanner,
      form,
      errorEditMessage,
      loadingEditSubmit,
      onValidate,
      onPickImage,
      handleDeleteBanner,
      loading,
      loadingDelete,
      errorMessage,
      dataHasChanged,
    };
  },
});
</script>

<template>
  <div v-if="!loading && !errorMessage" class="py-4 space-y-4">
    <div>
      <div class="mb-4">
        <router-link :to="back as string">
          <button class="flex items-center gap-1 hover:text-indigo-700">
            <font-awesome-icon icon="chevron-left" class="w-7 h-7" />
            <p class="text-base">Back</p>
          </button>
        </router-link>
      </div>
      <h1 class="text-center text-2xl lg:text-3xl font-semibold">
        {{ banner?.bannerName }}
      </h1>
    </div>
    <a
      v-if="banner?.link"
      :href="banner?.link"
      target="_blank"
      class="h-[200px] md:h-[300px] lg:h-[400px] w-full rounded ring-1 ring-slate-200 bg-slate-50 flex items-center justify-center overflow-auto"
    >
      <img
        :src="`${imageBaseUrl}${banner?.imageUrl}`"
        :style="`width: ${banner?.imageWidth}px; height: ${banner?.imageHeight}px;`"
      />
    </a>
    <div
      v-else
      class="h-[200px] md:h-[300px] lg:h-[400px] w-full rounded ring-1 ring-slate-200 bg-slate-50 flex items-center justify-center overflow-auto"
    >
      <img
        :src="`${imageBaseUrl}${banner?.imageUrl}`"
        class="object-contain w-full h-full"
        :style="`width: ${banner?.imageWidth}px; height: ${banner?.imageHeight}px;`"
      />
    </div>
    <div>
      <div class="flex flex-wrap gap-3 p-2 bg-slate-100">
        <button
          @click="handleChangeTab('banner-information')"
          :class="[
            tab === 'banner-information'
              ? 'cursor-default bg-indigo-600 text-white'
              : 'hover:bg-indigo-200',
            'py-2 px-4 rounded-md',
          ]"
        >
          Banner Information
        </button>
        <button
          @click="handleChangeTab('edit-banner')"
          :class="[
            tab === 'edit-banner'
              ? 'cursor-default bg-indigo-600 text-white'
              : 'hover:bg-indigo-200',
            'py-2 px-4 rounded-md',
          ]"
        >
          Edit banner
        </button>
        <button
          @click="handleChangeTab('delete-banner')"
          :class="[
            tab === 'delete-banner'
              ? 'cursor-default bg-indigo-600 text-white'
              : 'hover:bg-indigo-200',
            'py-2 px-4 rounded-md',
          ]"
        >
          Delete banner
        </button>
      </div>

      <div
        v-if="tab === 'banner-information'"
        class="border p-4 border-slate-100"
      >
        <div class="pb-4 border-b">
          <p class="text-base font-semibold">{{ banner?.bannerName }}</p>
          <p class="text-slate-500">
            {{ banner?.description ? banner.description : "No description" }}
          </p>
        </div>
        <div class="py-4 border-t">
          <p>
            Image:
            <a
              class="text-indigo-600 hover:underline"
              :href="`${imageBaseUrl}${banner?.imageUrl}`"
              target="_blank"
              >View</a
            >
          </p>
          <p>
            Size: {{ banner?.imageWidth ?? "?" }}×{{
              banner?.imageHeight ?? "?"
            }}
            px
          </p>
          <p>Views: {{ banner?.views ?? 0 }}</p>
          <p>Clicks: {{ banner?.clicks ?? 0 }}</p>
          <p>
            Link:
            <a
              v-if="banner?.link"
              :href="banner?.link"
              target="_blank"
              class="text-indigo-600 hover:underline"
              >open</a
            >
            <span v-else>None</span>
          </p>
        </div>
        <div class="py-4 border-t">
          <div class="mb-2">
            <p class="font-semibold mb-2">Project</p>
            <router-link
              :to="`/projects/${banner?.projectId}?back=${currentPath}`"
              class="hover:bg-slate-200 flex items-center gap-2 py-2"
            >
              <img
                :src="
                  banner?.projectImageUrl
                    ? imageBaseUrl + banner.projectImageUrl
                    : '/images/total/folder-icon.png'
                "
                alt="Campaign Image"
                class="object-cover h-16 w-16 rounded-lg"
              />
              <div>
                <p>Project: {{ banner?.projectName }}</p>
                <p class="line-clamp-3 text-xs text-slate-400">
                  {{
                    banner?.projectDescription
                      ? banner?.projectDescription
                      : "No description"
                  }}
                </p>
              </div>
            </router-link>
          </div>
        </div>
        <div class="py-4 border-t">
          <div class="mb-2">
            <p class="font-semibold mb-2">Campaign</p>
            <router-link
              :to="`/campaigns/${banner?.campaignId}?back=${currentPath}`"
              class="hover:bg-slate-200 flex items-center gap-2 py-2"
            >
              <img
                :src="
                  banner?.campaignImageUrl
                    ? imageBaseUrl + banner.campaignImageUrl
                    : '/images/total/folder-icon.png'
                "
                alt="Campaign Image"
                class="object-cover h-16 w-16 rounded-lg"
              />
              <div>
                <p>Campaign: {{ banner?.campaignName }}</p>
                <p class="line-clamp-3 text-xs text-slate-400">
                  {{
                    banner?.campaignDescription
                      ? banner?.campaignDescription
                      : "No description"
                  }}
                </p>
              </div>
            </router-link>
          </div>
        </div>
      </div>

      <div v-if="tab === 'edit-banner'" class="border p-4 border-slate-100">
        <div class="">
          <form class="space-y-4" @submit.prevent="submitUpdateBanner">
            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Banner Name <span class="text-indigo-500">*</span></label
              >
              <input
                type="text"
                placeholder="e.g. Homepage 300x250"
                class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                v-model="form.bannerName"
              />
            </div>

            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Description</label
              >
              <textarea
                rows="3"
                class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                v-model="form.description"
                placeholder="Optional notes about this banner"
              />
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-slate-600"
                  >Banner Image <span class="text-indigo-500">*</span></label
                >
                <input
                  type="file"
                  accept="image/*"
                  @change="onPickImage"
                  class="block w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                />
              </div>
              <div>
                <div class="mb-1 text-sm text-slate-600">Preview</div>
                <div
                  class="h-[200px] w-full bg-slate-100 ring-1 ring-slate-200 overflow-auto"
                >
                  <img
                    v-if="form.imageUrlPreview"
                    :src="
                      typeof form.imageUrl !== 'string'
                        ? form.imageUrlPreview
                        : `${imageBaseUrl}${form.imageUrlPreview}`
                    "
                    :style="`width: ${form.imageWidth}px; height: ${form.imageHeight}px;`"
                    :alt="form.bannerName"
                  />
                  <span v-else class="text-slate-400 text-sm"
                    >No image selected</span
                  >
                </div>
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div>
                <label class="mb-1 block text-sm text-slate-600"
                  >Image Width (px)
                  <span class="text-indigo-500">*</span></label
                >
                <input
                  type="number"
                  min="0"
                  class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                  v-model.number="form.imageWidth"
                />
              </div>
              <div>
                <label class="mb-1 block text-sm text-slate-600"
                  >Image Height (px)
                  <span class="text-indigo-500">*</span></label
                >
                <input
                  type="number"
                  min="0"
                  class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                  v-model.number="form.imageHeight"
                />
              </div>
            </div>

            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Click-through URL</label
              >
              <input
                type="url"
                placeholder="https://example.com/landing"
                class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                v-model="form.link"
              />
            </div>

            <p v-if="errorEditMessage" class="text-rose-600 text-sm">
              {{ errorEditMessage }}
            </p>

            <div class="flex justify-end items-center gap-3 pt-4">
              <button
                v-if="loadingEditSubmit"
                type="submit"
                :disabled="loadingEditSubmit || !onValidate()"
                class="w-full lg:w-auto rounded-lg bg-indigo-400 px-4 py-2 text-white flex items-center gap-2"
              >
                <loader customClass="w-7 h-7" /> Loading
              </button>
              <button
                v-else
                type="submit"
                :disabled="
                  loadingEditSubmit || !onValidate() || !dataHasChanged()
                "
                class="w-full lg:w-auto rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-500 disabled:opacity-60"
              >
                Update Banner
              </button>
            </div>
          </form>
        </div>
      </div>

      <div v-if="tab === 'delete-banner'" class="border p-4 border-slate-100">
        <div class="flex flex-col gap-4 items-center">
          <p>Delete the banner</p>
          <button
            @click="handleDeleteBanner(banner?.bannerId)"
            class="bg-red-600 hover:bg-red-700 px-6 py-2 rounded-md text-white"
          >
            Delete
          </button>
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
</template>
