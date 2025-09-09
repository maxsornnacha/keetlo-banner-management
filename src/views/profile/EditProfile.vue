<script lang="ts">
import Loader from "@/components/ui/Loader.vue";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { useAlert } from "@/utils/use-alert";
import { defineComponent, onMounted, ref, watch } from "vue";

export default defineComponent({
  name: "EditProfilePage",
  components: {
    Loader,
  },
  setup() {
    type UserEdit = {
      firstname: string | null;
      lastname: string | null;
      avatarUrl: string | null;
    };
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const original = ref<UserEdit | null>(null);
    const form = ref<UserEdit>({
      firstname: null,
      lastname: null,
      avatarUrl: null,
    });
    const errorMessage = ref<string | null>(null);
    const serverOk = ref(false);
    const now = new Date().toLocaleString();
    const dirty = ref(false);
    watch(
      form,
      () => {
        dirty.value = true;
        serverOk.value = false;
      },
      { deep: true }
    );
    const avatarInput = ref<HTMLInputElement | null>(null);
    const avatarPreview = ref<string | null>(null);
    const canSubmit = ref(false);
    const { openAlert, closeAlert } = useAlert();
    const loadingSubmit = ref(false);
    const loading = ref(true);

    function pickAvatar() {
      avatarInput.value?.click();
    }
    function onAvatarPicked(e: Event) {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (!file) return;
      if (file.size > 2 * 1024 * 1024) {
        openAlert("File too large (max 2MB).", "warn");
        setTimeout(() => {
          closeAlert();
        }, 1500);
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        avatarPreview.value = String(reader.result);
        form.value.avatarUrl = String(reader.result);
      };
      reader.readAsDataURL(file);
    }
    function removeAvatar() {
      avatarPreview.value = null;
      form.value.avatarUrl = original.value?.avatarUrl || null;
    }

    watch(
      form,
      () => {
        const hasNames = !!form.value.firstname && !!form.value.lastname;
        const changed =
          JSON.stringify(form.value) !== JSON.stringify(original.value);
        canSubmit.value = hasNames && changed;
      },
      { deep: true, immediate: true }
    );

    const fetchUser = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/user/me`
        );
        const deep = JSON.parse(JSON.stringify(responseData)) as UserEdit;
        form.value = deep;
        original.value = { ...deep };
        loading.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loading.value = false;
      }
    };

    onMounted(async () => {
      try {
        loadingSubmit.value = true;
        await fetchUser();
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
      } finally {
        loadingSubmit.value = false;
      }
    });

    // --- actions ---
    const back = () => {
      window.location.href = "/profile";
    };

    const onSubmit = async () => {
      if (!canSubmit.value || loadingSubmit.value) return;

      loadingSubmit.value = true;
      errorMessage.value = null;
      try {
        const payload = {
          firstname: form.value.firstname,
          lastname: form.value.lastname,
          avatarUrl: avatarPreview.value ?? form.value.avatarUrl,
        };
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/user/me`,
          "PUT",
          {},
          payload
        );
        await fetchUser();
        avatarPreview.value = null;
        serverOk.value = true;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
      } finally {
        loadingSubmit.value = false;
      }
    };

    return {
      now,
      pickAvatar,
      onAvatarPicked,
      removeAvatar,
      back,
      onSubmit,
      errorMessage,
      serverOk,
      dirty,
      loadingSubmit,
      canSubmit,
      avatarInput,
      avatarPreview,
      form,
      imageBaseUrl,
      loading,
    };
  },
});
</script>
<template>
  <div class="lg:p-4">
    <!-- Header -->
    <header class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-semibold text-slate-800">Edit Profile</h1>
      <div class="text-sm text-slate-500">Updated: {{ now }}</div>
    </header>

    <!-- Alerts -->
    <div
      v-if="errorMessage"
      class="mb-4 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-rose-700 text-sm"
    >
      {{ errorMessage }}
    </div>
    <div
      v-if="serverOk"
      class="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-emerald-700 text-sm"
    >
      Profile updated successfully.
    </div>

    <!-- Card -->
    <div
      v-if="!loading"
      class="rounded-2xl bg-white lg:p-4 lg:shadow-sm lg:ring-1 lg:ring-slate-200"
    >
      <div class="grid gap-6 lg:grid-cols-3">
        <!-- Avatar -->
        <section>
          <h2 class="mb-3 text-base font-semibold text-slate-800">Avatar</h2>
          <div class="flex items-start gap-4">
            <img
              :src="
                avatarPreview
                  ? avatarPreview
                  : form?.avatarUrl
                  ? `${imageBaseUrl}${form.avatarUrl}`
                  : '/images/profile/profile.jpg'
              "
              alt="avatar"
              class="h-24 w-24 rounded-full object-cover ring-2 ring-slate-200"
            />
            <div class="space-y-2 text-sm">
              <div class="flex gap-1 items-center">
                <button
                  class="rounded-md border border-slate-300 px-3 py-1.5 hover:bg-slate-100"
                  type="button"
                  @click="pickAvatar"
                >
                  Upload
                </button>
                <button
                  class="rounded-md border border-slate-300 px-3 py-1.5 hover:bg-slate-100"
                  type="button"
                  @click="removeAvatar"
                  :disabled="!form.avatarUrl && !avatarPreview"
                >
                  Remove
                </button>
                <input
                  ref="avatarInput"
                  type="file"
                  class="hidden"
                  accept="image/*"
                  @change="onAvatarPicked"
                />
              </div>
              <p class="text-xs text-slate-500">
                PNG/JPG up to 2MB is recommended.
              </p>
            </div>
          </div>
        </section>

        <!-- Account -->
        <section class="lg:col-span-2">
          <h2 class="mb-3 text-base font-semibold text-slate-800">Account</h2>
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <label
                for="firstname"
                class="block text-sm font-medium text-gray-700"
                >Firstname</label
              >
              <input
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                v-model="form.firstname"
              />
            </div>
            <div>
              <label
                for="lastname"
                class="block text-sm font-medium text-gray-700"
                >Lastname</label
              >
              <input
                class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                v-model="form.lastname"
              />
            </div>
          </div>

          <!-- Actions -->
          <div
            class="mt-6 flex flex-col lg:flex-row w-full items-center justify-end gap-2"
          >
            <button
              class="w-full lg:w-auto rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100"
              type="button"
              @click="back"
              :disabled="loadingSubmit"
            >
              Cancel
            </button>
            <button
              :class="[
                'w-full lg:w-auto rounded-md px-3 py-1.5 text-sm text-white',
                canSubmit && !loadingSubmit
                  ? ' bg-indigo-600 hover:bg-indigo-700'
                  : ' bg-indigo-400',
              ]"
              type="button"
              @click="onSubmit"
              :disabled="loadingSubmit || !canSubmit"
            >
              <span v-if="loadingSubmit" class="flex gap-2 items-center"
                ><Loader customClass="w-5 h-5 !fill-indigo-400 !text-white" />
                Loading</span
              ><span v-else>Save Changes</span>
            </button>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
