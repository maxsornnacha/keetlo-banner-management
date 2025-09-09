<script lang="ts">
import { defineComponent, ref } from "vue";
import { fetchAPI } from "@/utils/fetch-api";
import { useRoute, useRouter } from "vue-router";
import { useConfirm } from "@/utils/use-confirm";
import { useAlert } from "@/utils/use-alert";
import Loader from "@/components/ui/Loader.vue";

export default defineComponent({
  name: "CreateProject",
  components: {
    Loader,
  },
  setup() {
    const confirm = useConfirm();
    const route = useRoute();
    const router = useRouter();
    type Form = {
      projectName: string;
      description: string;
      status: string;
      projectImageUrl: string | ArrayBuffer | null | undefined;
    };
    const form = ref<Form>({
      projectName: "",
      description: "",
      status: "active",
      projectImageUrl: null,
    });
    const imagePreview = ref<string | ArrayBuffer | null | undefined>(null);
    const error = ref<string | null>(null);
    const back = route.query.display_project_selection
      ? ((route.query.back +
          `&display_project_selection=${route.query.display_project_selection}`) as string)
      : route.query.back || "/projects";
    const { openAlert, closeAlert } = useAlert();
    const loadingSubmit = ref(false);

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
      error.value = null;
      if (!form.value.projectName) {
        error.value = "Project Name is required.";
        return;
      }

      const status = await confirm("Would you like to create a new project");
      if (!status) {
        return;
      }

      try {
        loadingSubmit.value = true;
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/project/create`,
          "POST",
          {},
          form.value
        );
        openAlert("Project created successfully!", "success");
        setTimeout(() => {
          router.push(back as string);
          closeAlert();
        }, 1500);
      } catch (err) {
        error.value = "Failed to create project. Please try again.";
        console.error(err);
        loadingSubmit.value = false;
      }
    };

    return {
      form,
      imagePreview,
      error,
      handleImageChange,
      handleSubmit,
      back,
      loadingSubmit,
    };
  },
});
</script>
<template>
  <div class="max-w-lg mx-auto lg:p-8 lg:bg-white lg:shadow-md rounded-md">
    <h2 class="text-2xl font-semibold text-gray-700 mb-6">
      Create New Project
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
        <label for="projectName" class="block text-sm font-medium text-gray-700"
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
        <label for="description" class="block text-sm font-medium text-gray-700"
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
        <router-link :to="back as string">
          <button
            class="w-full px-4 py-2 border border-slate-200 rounded-md hover:bg-gray-200"
          >
            Back
          </button>
        </router-link>
        <button
          v-if="!loadingSubmit"
          type="submit"
          class="w-full px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700"
        >
          Create Project
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

    <div v-if="error" class="mt-4 text-red-500">
      <p>{{ error }}</p>
    </div>
  </div>
</template>
