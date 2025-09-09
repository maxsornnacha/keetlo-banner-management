<!-- src/views/BannerCreatePage.vue -->
<script lang="ts">
import Autocomplete from "@/components/ui/Autocomplete.vue";
import Loader from "@/components/ui/Loader.vue";
import { Campaign } from "@/types/Campaign";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useAlert } from "@/utils/use-alert";
import { useConfirm } from "@/utils/use-confirm";
import { defineComponent, ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

type BannerForm = {
  campaignId: string;
  bannerName: string;
  description: string;
  imageFile: File | null;
  imageUrlPreview: string | null;
  imageWidth: number | null;
  imageHeight: number | null;
  link: string;
};

export default defineComponent({
  name: "BannerCreatePage",
  components: {
    Autocomplete,
    Loader,
  },
  setup() {
    const confirm = useConfirm();
    const route = useRoute();
    const router = useRouter();
    const back = computed<string>(() => {
      const q = route.query.back;
      const back = Array.isArray(q) ? q[0] : q;
      return back && String(back).trim() ? String(back) : "/projects";
    });
    const displayCampaignSelection = ref(
      route.query.display_campaign_selection
        ? route.query.display_campaign_selection
        : false
    );
    const selectedCampaignId = ref<string>("");
    const campaignOptions = ref([]);
    const currentPath = route.fullPath;
    const loadingSubmit = ref(false);
    const loadingOption = ref(true);
    const campaignId = computed<string>(() => {
      const q = route.query.campaign_id;
      const id = Array.isArray(q) ? q[0] : q;
      return (id && String(id)) || "";
    });
    const form = ref<BannerForm>({
      campaignId: campaignId.value,
      bannerName: "",
      description: "",
      imageFile: null,
      imageUrlPreview: null,
      imageWidth: null,
      imageHeight: null,
      link: "",
    });
    const errorMessage = ref<string | null>(null);
    const { openAlert, closeAlert } = useAlert();

    const fetchcampaignOptionApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/campaign/options`
        );
        if (responseData && responseData.options) {
          campaignOptions.value = responseData.options.map(
            (item: Campaign) => ({
              label: `${item.campaignName}->project:${item.projectName}`,
              value: item.campaignId,
            })
          );
        }
        loadingOption.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loadingOption.value = false;
      }
    };

    onMounted(async () => {
      if (displayCampaignSelection.value === "1") {
        if (route.query.project_id) {
          selectedCampaignId.value = route.query.campaign_id as string;
        } else {
          await fetchcampaignOptionApi();
        }
      }
    });

    const onPickImage = (e: Event) => {
      const file = (e.target as HTMLInputElement).files?.[0] || null;
      form.value.imageFile = file;
      form.value.imageUrlPreview = null;
      form.value.imageWidth = null;
      form.value.imageHeight = null;

      if (!file) return;
      const objectUrl = URL.createObjectURL(file);
      form.value.imageUrlPreview = objectUrl;

      const img = new Image();
      img.onload = () => {
        form.value.imageWidth = img.naturalWidth || null;
        form.value.imageHeight = img.naturalHeight || null;
        URL.revokeObjectURL(objectUrl);
      };
      img.onerror = () => URL.revokeObjectURL(objectUrl);
      img.src = objectUrl;
    };

    const validate = (): string | null => {
      if (!form.value.campaignId) return "Campaign ID not found.";
      if (!form.value.imageFile) return "Please select a banner image.";
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
      if (!form.value.campaignId) return false;
      if (!form.value.imageFile) return false;
      if (!form.value.bannerName) return false;
      if (!form.value.imageWidth) return false;
      if (!form.value.imageHeight) return false;
      return true;
    };

    const fileToBase64 = (file: File): Promise<string> => {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result as string);
        reader.onerror = reject;
        reader.readAsDataURL(file); // "data:image/png;base64,...."
      });
    };

    watch(selectedCampaignId, (newId) => {
      router.replace({
        query: {
          ...route.query,
          campaign_id: newId,
        },
      });
      form.value.campaignId = newId;
    });

    const submit = async () => {
      errorMessage.value = null;
      const v = validate();
      if (v) {
        errorMessage.value = v;
        return;
      }
      const status = await confirm("Would you like to create a new banner?");
      if (!status) {
        return;
      }

      try {
        loadingSubmit.value = true;

        let base64Image: string | null = null;
        if (form.value.imageFile) {
          base64Image = await fileToBase64(form.value.imageFile);
        }

        const payload = {
          campaignId: form.value.campaignId,
          bannerName: form.value.bannerName,
          description: form.value.description,
          imageUrl: base64Image,
          imageWidth: form.value.imageWidth,
          imageHeight: form.value.imageHeight,
          link: form.value.link ? form.value.link : null,
        };
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/banner/create`,
          "POST",
          {},
          payload
        );
        loadingSubmit.value = false;
        openAlert("Banner created successfully!", "success");
        setTimeout(() => {
          router.push(back.value);
          closeAlert();
        }, 1500);
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loadingSubmit.value = false;
      }
    };

    return {
      back,
      form,
      loadingSubmit,
      errorMessage,
      onPickImage,
      submit,
      onValidate,
      campaignOptions,
      selectedCampaignId,
      displayCampaignSelection,
      currentPath,
      loadingOption,
    };
  },
});
</script>

<template>
  <div class="flex justify-center w-full">
    <div class="w-full lg:w-auto">
      <!-- Header -->
      <header class="mb-6 flex items-center gap-4">
        <router-link :to="back">
          <button class="flex items-center gap-1 hover:text-indigo-700">
            <font-awesome-icon icon="chevron-left" class="w-7 h-7" />
            <p class="text-base">Back</p>
          </button>
        </router-link>
        <h1 class="text-2xl font-semibold text-slate-800">Create Banner</h1>
      </header>

      <!-- Card -->
      <div
        class="rounded-2xl bg-white lg:p-4 lg:shadow-sm lg:ring-1 lg:ring-slate-200 lg:max-w-3xl"
      >
        <form class="space-y-4" @submit.prevent="submit">
          <!-- Project Selection -->
          <div
            v-if="displayCampaignSelection && displayCampaignSelection === '1'"
            class="mb-4"
          >
            <div v-if="loadingOption">
              <Loader customClass="w-7 h-7" />
            </div>
            <div v-else>
              <label
                for="projectSelection"
                class="block text-sm font-medium text-gray-700"
                >Camapigns <span class="text-indigo-500">*</span></label
              >
              <Autocomplete
                v-if="campaignOptions && campaignOptions.length > 0"
                v-model="selectedCampaignId"
                :options="campaignOptions"
                placeholder="Please select the campaign of this banner"
              />
              <p
                v-if="!campaignOptions || campaignOptions.length === 0"
                class="p-2 border border-slate-200 rounded-md bg-gray-100"
              >
                There are no campaigns yet. Please go to create campaign first.
              </p>
              <div class="my-2">
                <router-link
                  :to="`/campaigns/create?back=${currentPath}&display_project_selection=1`"
                  class="bg-white hover:bg-gray-200 border border-slate-300 p-1 rounded-md"
                  >Create Campaign</router-link
                >
              </div>
            </div>
          </div>
          <!-- Banner name (schema ล่าสุดมีคอลัมน์นี้) -->
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

          <!-- Description -->
          <div>
            <label class="mb-1 block text-sm text-slate-600">Description</label>
            <textarea
              rows="3"
              class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
              v-model="form.description"
              placeholder="Optional notes about this banner"
            />
          </div>

          <!-- Image picker + preview -->
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
                  :src="form.imageUrlPreview"
                  :alt="form.bannerName"
                  :style="`width: ${form.imageWidth}px; height: ${form.imageHeight}px;`"
                />
                <span v-else class="text-slate-400 text-sm"
                  >No image selected</span
                >
              </div>
            </div>
          </div>

          <!-- Detected size -->
          <div class="grid gap-4 md:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Image Width (px) <span class="text-indigo-500">*</span></label
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
                >Image Height (px) <span class="text-indigo-500">*</span></label
              >
              <input
                type="number"
                min="0"
                class="w-full rounded-md border border-slate-300 px-3 py-2 text-slate-800"
                v-model.number="form.imageHeight"
              />
            </div>
          </div>

          <!-- Click-through URL -->
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

          <!-- Error -->
          <p v-if="errorMessage" class="text-rose-600 text-sm">
            {{ errorMessage }}
          </p>

          <!-- Submit -->
          <div class="flex justify-end items-center gap-3 pt-4">
            <button
              v-if="loadingSubmit"
              type="submit"
              :disabled="loadingSubmit || !onValidate()"
              class="w-full lg:w-auto rounded-lg bg-indigo-400 px-4 py-2 text-white flex items-center gap-2"
            >
              <loader customClass="w-7 h-7" /> Loading
            </button>
            <button
              v-else
              type="submit"
              :disabled="loadingSubmit || !onValidate()"
              class="w-full lg:w-auto rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              Create Banner
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Tailwind ใช้เป็นหลัก */
</style>
