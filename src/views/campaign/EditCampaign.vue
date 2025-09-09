<script lang="ts">
import { defineComponent, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useConfirm } from "@/utils/use-confirm";
import { useAlert } from "@/utils/use-alert";
import Loader from "@/components/ui/Loader.vue";

export default defineComponent({
  name: "EditCampaign",
  components: {
    Loader,
  },
  setup() {
    const confirm = useConfirm();
    const getTodayDate = () => {
      const today = new Date();
      const year = today.getFullYear();
      const month = (today.getMonth() + 1).toString().padStart(2, "0");
      const day = today.getDate().toString().padStart(2, "0");
      return `${year}-${month}-${day}`;
    };
    const route = useRoute();
    const router = useRouter();
    const slug = route.params.slug;
    type Form = {
      campaignName: string;
      description: string;
      status: string;
      campaignImageUrl: string | ArrayBuffer | null | undefined;
      startDate: string | null;
      endDate: string | null;
      hasEndDate: boolean;
    };
    const original = ref<Form>({
      campaignName: "",
      description: "",
      status: "active",
      campaignImageUrl: null,
      startDate: getTodayDate() || null,
      endDate: null,
      hasEndDate: false,
    });
    const form = ref<Form>({
      campaignName: "",
      description: "",
      status: "active",
      campaignImageUrl: null,
      startDate: getTodayDate() || null,
      endDate: null,
      hasEndDate: false,
    });
    const imagePreview = ref<string | ArrayBuffer | null | undefined>(null);
    const errorMessage = ref<string | null>(null);
    const back = (route.query.back as string) || `/campaigns/${slug}`;
    const { openAlert, closeAlert } = useAlert();
    const loading = ref(true);
    const loadingSubmit = ref(false);
    

    onMounted(async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/campaign/update/${slug}`
        );
        const deep = JSON.parse(JSON.stringify(responseData.campaign));
        form.value = deep;
        original.value = { ...deep };
        if (responseData?.campaign?.campaignImageUrl) {
          imagePreview.value =
            process.env.VUE_APP_API_URL +
            responseData?.campaign?.campaignImageUrl;
        }
        if (responseData?.campaign?.endDate) {
          form.value.hasEndDate = true;
          form.value.endDate = responseData?.campaign?.endDate || null;
        }
        loading.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loading.value = false;
      }
    });

    const handleImageChange = (event: Event) => {
      const file = (event.target as HTMLInputElement).files?.[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e: ProgressEvent<FileReader>) => {
          form.value.campaignImageUrl = e?.target?.result;
          imagePreview.value = e?.target?.result;
        };
        reader.readAsDataURL(file);
      }
    };

    watch(
      () => form.value.hasEndDate,
      (hasEndDate) => {
        if (!hasEndDate) {
          form.value.endDate = null;
        }
      }
    );

    watch(
      () => form.value.startDate,
      (startDate) => {
        if (!startDate) {
          form.value.startDate = null;
        }
      }
    );

    watch(
      () => form.value.endDate,
      (endDate) => {
        if (!endDate) {
          console.log(endDate);
          form.value.endDate = null;
        }
      }
    );

    const handleSubmit = async () => {
      errorMessage.value = null;
      if (!form.value.campaignName) {
        errorMessage.value = "Campaign Name is required.";
        return;
      }

      const status = await confirm("Would you like to update a new campaign?");
      if (!status) {
        return;
      }

      try {
        loadingSubmit.value = true;
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/campaign/update/${slug}`,
          "PUT",
          {},
          form.value
        );
        openAlert("Campaign updated successfully!", "success");
        setTimeout(() => {
          router.push(back);
          closeAlert();
        }, 1500);
      } catch (error) {
        errorMessage.value = toErrorMessage(error);
        loadingSubmit.value = false;
      }
    };

    const minEndDate = () => {
      if (form.value.startDate) {
        const startDate = new Date(form.value.startDate);
        startDate.setDate(startDate.getDate() + 1);
        const year = startDate.getFullYear();
        const month = (startDate.getMonth() + 1).toString().padStart(2, "0");
        const day = startDate.getDate().toString().padStart(2, "0");
        return `${year}-${month}-${day}`;
      }
      return "";
    };

    const dataHasChanged = () => {
      return !(
        original.value.campaignImageUrl === form.value.campaignImageUrl &&
        original.value.campaignName === form.value.campaignName &&
        original.value.description === form.value.description &&
        original.value.endDate === form.value.endDate &&
        original.value.startDate === form.value.startDate &&
        original.value.status === form.value.status
      );
    };

    return {
      form,
      imagePreview,
      errorMessage,
      handleImageChange,
      handleSubmit,
      back,
      todayDate: getTodayDate(),
      minEndDate,
      dataHasChanged,
      loadingSubmit,
      loading,
    };
  },
});
</script>
<template>
  <div>
  <div
  v-if="!loading && !errorMessage"
    class="max-w-lg mx-auto lg:p-8 lg:bg-white lg:shadow-md rounded-md"
  >
    <h2 class="text-2xl font-semibold text-gray-700 mb-6">
      Update The Campaign
    </h2>

    <form @submit.prevent="handleSubmit">
      <!-- Campaign Icon -->
      <div class="mb-4">
        <label
          for="campaign_image"
          class="block text-sm font-medium text-gray-700"
          >Campaign Icon</label
        >
        <input
          id="campaign_image"
          type="file"
          @change="handleImageChange"
          class="w-full p-2 mt-2 border border-gray-300 rounded-md"
          accept="image/*"
        />
        <div class="flex justify-center">
          <img
            v-if="imagePreview"
            :src="imagePreview as string"
            class="mt-2 w-40 h-40 object-cover rounded-md"
            alt="Image preview"
          />
          <img
            v-if="!imagePreview"
            src="/images/total/folder-icon.png"
            class="mt-2 w-40 h-40 object-cover rounded-md"
            alt="Image preview"
          />
        </div>
      </div>

      <!-- Campaign Name -->
      <div class="mb-4">
        <label
          for="campaignName"
          class="block text-sm font-medium text-gray-700"
          >Campaign Name <span class="text-indigo-500">*</span></label
        >
        <input
          id="campaignName"
          v-model="form.campaignName"
          type="text"
          class="w-full p-2 mt-2 border border-gray-300 rounded-md"
          placeholder="Enter campaign name"
          required
        />
      </div>

      <!-- Description -->
      <div class="mb-4">
        <label for="description" class="block text-sm font-medium text-gray-700"
          >Description</label
        >
        <textarea
          id="description"
          v-model="form.description"
          class="w-full p-2 mt-2 border border-gray-300 rounded-md"
          placeholder="Enter campaign description"
        ></textarea>
      </div>

      <!-- Start Date -->
      <div class="mb-4 grid grid-cols-1 gap-2">
        <div>
          <label for="startDate" class="block text-sm font-medium text-gray-700"
            >Start Date <span class="text-indigo-500">*</span></label
          >
          <input
            required
            type="date"
            v-model="form.startDate"
            class="border border-slate-200 p-2 rounded-md w-full"
            :min="todayDate"
          />
        </div>

        <!-- End Date Checkbox -->
        <div class="mb-4">
          <label class="inline-flex items-center">
            <input type="checkbox" v-model="form.hasEndDate" class="mr-2" />
            <span class="text-sm text-gray-700">Set an end date</span>
          </label>
        </div>

        <!-- End Date (Only if the checkbox is checked) -->
        <div v-if="form.hasEndDate">
          <label for="endDate" class="block text-sm font-medium text-gray-700"
            >End Date</label
          >
          <input
            :disabled="form.startDate ? false : true"
            type="date"
            v-model="form.endDate"
            class="border border-slate-200 p-2 rounded-md w-full"
            :min="minEndDate()"
          />
        </div>
      </div>

      <!-- Status -->
      <div class="mb-4">
        <label for="status" class="block text-sm font-medium text-gray-700"
          >Status <span class="text-indigo-500">*</span></label
        >
        <select
          id="status"
          v-model="form.status"
          class="w-full p-2 mt-2 border border-gray-300 rounded-md"
        >
          <option value="active">Active</option>
          <option value="inactive">Inactive</option>
          <option value="completed">Completed</option>
        </select>
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-col gap-2">
        <router-link :to="back">
          <button
            class="w-full px-4 py-2 border border-slate-200 rounded-md hover:bg-gray-200"
          >
            Back
          </button>
        </router-link>
        <button
          v-if="!loadingSubmit"
          type="submit"
          :disabled="loadingSubmit || !dataHasChanged()"
          :class="[
            !dataHasChanged()
              ? 'w-full px-4 py-2 text-white rounded-md bg-indigo-400 flex justify-center items-center gap-2'
              : 'w-full px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700',
          ]"
        >
          Update Campaign
        </button>
        <button
          v-else
          disabled
          type="submit"
          class="w-full px-4 py-2 text-white rounded-md bg-indigo-400 flex justify-center items-center gap-2"
        >
          <Loader customClass="fill-indigo-400 text-white w-5 h-5" />
          Loading
        </button>
      </div>
    </form>
  </div>
      <!-- Error Message -->
    <div v-if="errorMessage" class="mt-4 text-red-500">
      <p>{{ errorMessage }}</p>
    </div>
  </div>
</template>
