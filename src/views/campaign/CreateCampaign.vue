<script lang="ts">
import { defineComponent, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import Autocomplete from "@/components/ui/Autocomplete.vue";
import { Project } from "@/types/Project";
import { useConfirm } from "@/utils/use-confirm";
import { useAlert } from "@/utils/use-alert";
import Loader from "@/components/ui/Loader.vue";

export default defineComponent({
  name: "CreateCampaign",
  components: {
    Autocomplete,
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
    const displayProjectSelection = ref(
      route.query.display_project_selection
        ? route.query.display_project_selection
        : false
    );
    const projectId = ref(
      route.query.project_id ? (route.query.project_id as string) : null
    );
    const selectedProjectId = ref<string>("");
    const projectOptions = ref([]);
    type Form = {
      projectId: typeof projectId;
      campaignName: string;
      description: string;
      status: string;
      campaignImageUrl: string | ArrayBuffer | null | undefined;
      startDate: string | null;
      endDate: string | null;
      hasEndDate: boolean;
    };

    const form = ref<Form>({
      projectId: projectId,
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
    const back = route.query.display_campaign_selection
      ? ((route.query.back +
          `&display_campaign_selection=${route.query.display_campaign_selection}`) as string)
      : route.query.back || "/campaigns";
    const { openAlert, closeAlert } = useAlert();
    const currentPath = route.fullPath;
    const loadingSubmit = ref(false);
    const loadingOption = ref(true);

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

    const fetchprojectOptionApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/project/options`
        );
        if (responseData && responseData.options) {
          projectOptions.value = responseData.options.map((item: Project) => ({
            label: item.projectName,
            value: item.projectId,
          }));
          loadingOption.value = false;
        }
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loadingOption.value = false;
      }
    };

    onMounted(async () => {
      if (displayProjectSelection.value === "1") {
        if (route.query.project_id) {
          selectedProjectId.value = route.query.project_id as string;
        } else {
          await fetchprojectOptionApi();
        }
      }
    });

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
          form.value.endDate = null;
        }
      }
    );

    watch(selectedProjectId, (newId) => {
      router.replace({
        query: {
          ...route.query,
          project_id: newId,
        },
      });
      projectId.value = newId;
    });

    const handleSubmit = async () => {
      errorMessage.value = null;
      if (!form.value.projectId) {
        openAlert("Project Id not found, please try again", "error");
        setTimeout(() => {
          closeAlert();
        }, 1500);
        return;
      }

      if (!form.value.campaignName) {
        openAlert("Campaign Name is required.", "error");
        setTimeout(() => {
          closeAlert();
        }, 1500);
        return;
      }

      const status = await confirm("Would you like to create a new campaign?");
      if (!status) {
        return;
      }

      try {
        loadingSubmit.value = true;
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/campaign/create`,
          "POST",
          {},
          form.value
        );
        openAlert("Campaign created successfully!", "success");
        setTimeout(() => {
          router.push(back as string);
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

    return {
      form,
      imagePreview,
      errorMessage,
      handleImageChange,
      handleSubmit,
      back,
      todayDate: getTodayDate(),
      minEndDate,
      projectId,
      displayProjectSelection,
      selectedProjectId,
      projectOptions,
      currentPath,
      loadingSubmit,
      loadingOption,
    };
  },
});
</script>
<template>
  <div class="max-w-lg mx-auto lg:p-8 lg:bg-white lg:shadow-md rounded-md">
    <h2 class="text-2xl font-semibold text-gray-700 mb-6">
      Create New Campaign
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

      <!-- Project Selection -->
      <div
        v-if="displayProjectSelection && displayProjectSelection === '1'"
        class="mb-4"
      >
        <div v-if="loadingOption">
          <Loader customClass="w-7 h-7" />
        </div>
        <div v-else>
          <label
            for="projectSelection"
            class="block text-sm font-medium text-gray-700"
            >Project <span class="text-indigo-500">*</span></label
          >
          <Autocomplete
            v-if="projectOptions && projectOptions.length > 0"
            v-model="selectedProjectId"
            :options="projectOptions"
            placeholder="Please select the project of this campaign"
          />
          <p
            v-if="!projectOptions || projectOptions.length === 0"
            class="p-2 border border-slate-200 rounded-md bg-gray-100"
          >
            There are no projects yet. Please go to create project first.
          </p>
          <div class="my-2">
            <router-link
              :to="`/projects/create?back=${currentPath}`"
              class="bg-white hover:bg-gray-200 border border-slate-300 p-1 rounded-md"
              >Create Project</router-link
            >
          </div>
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
        <router-link :to="back as string">
          <button
            class="w-full px-4 py-2 border border-slate-200 rounded-md hover:bg-gray-200"
          >
            Back
          </button>
        </router-link>
        <button
          v-if="!loadingSubmit"
          :disabled="!projectId"
          type="submit"
          :class="[
            'w-full px-4 py-2 text-white rounded-md',
            projectId ? 'bg-indigo-600 hover:bg-indigo-700' : 'bg-indigo-400',
          ]"
        >
          Create Campaign
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

    <!-- Error Message -->
    <div v-if="errorMessage" class="mt-4 text-red-500">
      <p>{{ errorMessage }}</p>
    </div>
  </div>
</template>
