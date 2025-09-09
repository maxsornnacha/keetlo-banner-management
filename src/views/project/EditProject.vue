<script lang="ts">
import { defineComponent, onMounted, ref } from "vue";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useRoute, useRouter } from "vue-router";
import { useConfirm } from "@/utils/use-confirm";
import { useAlert } from "@/utils/use-alert";
import Loader from "@/components/ui/Loader.vue";

export default defineComponent({
  name: "EditProject",
  components: {
    Loader,
  },
  setup() {
    const confirm = useConfirm();
    const route = useRoute();
    const router = useRouter();
    const slug = route.params.slug;
    type Form = {
      projectName: string;
      description: string;
      status: string;
      projectImageUrl: string | ArrayBuffer | null | undefined;
    };
    const original = ref<Form>({
      projectName: "",
      description: "",
      status: "active",
      projectImageUrl: null,
    });
    const form = ref<Form>({
      projectName: "",
      description: "",
      status: "active",
      projectImageUrl: null,
    });
    const imagePreview = ref<string | ArrayBuffer | null | undefined>(null);
    const back = (route.query.back as string) || "/projects";
    const { openAlert, closeAlert } = useAlert();
    const errorMessage = ref<string | null>(null);
    const loading = ref(true);
    const loadingSubmit = ref(false);

    onMounted(async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/project/update/${slug}`
        );
        const deep = JSON.parse(JSON.stringify(responseData.project));
        form.value = deep;
        original.value = { ...deep };
        if (responseData?.project?.projectImageUrl) {
          imagePreview.value =
            process.env.VUE_APP_API_URL +
            responseData?.project?.projectImageUrl;
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
          form.value.projectImageUrl = e?.target?.result;
          imagePreview.value = e?.target?.result;
        };
        reader.readAsDataURL(file);
      }
    };

    const handleSubmit = async () => {
      errorMessage.value = null;
      if (!form.value.projectName) {
        errorMessage.value = "Project Name is required.";
        return;
      }

      const status = await confirm("Would you like to update a new project");
      if (!status) {
        return;
      }

      try {
        loadingSubmit.value = true;
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/project/update/${slug}`,
          "PUT",
          {},
          form.value
        );
        openAlert("Project updated successfully!", "success");
        setTimeout(() => {
          router.push(back);
          closeAlert();
        }, 1500);
      } catch (err) {
        errorMessage.value = "Failed to update project. Please try again.";
        console.error(err);
      }
    };

    const dataHasChanged = () => {
      return JSON.stringify(original.value) !== JSON.stringify(form.value);
    };

    return {
      form,
      imagePreview,
      errorMessage,
      handleImageChange,
      handleSubmit,
      back,
      loading,
      loadingSubmit,
      dataHasChanged,
    };
  },
});
</script>
<template>
  <div v-if="!loading && !errorMessage">
    <div class="max-w-lg mx-auto lg:p-8 lg:bg-white lg:shadow-md rounded-md">
      <h2 class="text-2xl font-semibold text-gray-700 mb-6">
        Update The Project
      </h2>

      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label
            for="project_image"
            class="block text-sm font-medium text-gray-700"
            >Project Icon</label
          >
          <input
            id="project_image"
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

        <div class="mb-4">
          <label
            for="projectName"
            class="block text-sm font-medium text-gray-700"
            >Project Name <span class="text-indigo-500">*</span></label
          >
          <input
            id="projectName"
            v-model="form.projectName"
            type="text"
            class="w-full p-2 mt-2 border border-gray-300 rounded-md"
            placeholder="Enter project name"
            required
          />
        </div>

        <div class="mb-4">
          <label
            for="description"
            class="block text-sm font-medium text-gray-700"
            >Description</label
          >
          <textarea
            id="description"
            v-model="form.description"
            class="w-full p-2 mt-2 border border-gray-300 rounded-md"
            placeholder="Enter project description"
          ></textarea>
        </div>

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

        <div class="flex flex-col gap-2">
          <router-link :to="back">
            <button
              :disabled="loadingSubmit"
              class="w-full px-4 py-2 border border-slate-200 rounded-md hover:bg-gray-200"
            >
              Back
            </button>
          </router-link>
          <button
            :disabled="loadingSubmit || !dataHasChanged()"
            v-if="!loadingSubmit"
            type="submit"
            :class="[
              !dataHasChanged()
                ? 'w-full px-4 py-2 text-white rounded-md bg-indigo-400 flex justify-center items-center gap-2'
                : 'w-full px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700',
            ]"
          >
            Update Project
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
    <div v-if="errorMessage" class="mt-4 text-red-500">
      <p>{{ errorMessage }}</p>
    </div>
  </div>
</template>
