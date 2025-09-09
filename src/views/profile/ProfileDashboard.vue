<script lang="ts">
import { useUserStore } from "@/store";
import { User } from "@/types/User";
import { formatDateTime } from "@/utils/date-handler";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { storeToRefs } from "pinia";
import { onMounted, ref, computed, defineComponent } from "vue";
const imageBaseUrl = process.env.VUE_APP_API_URL;

export default defineComponent({
  name: "ProfileDashbaordPage",
  setup() {
    const loading = ref(true);
    const errorMessage = ref<string | null>(null);
    const userData = ref<User>({
      userCode: "",
      username: "",
      email: "",
      firstname: null,
      lastname: null,
      avatarUrl: null,
      createdAt: "",
      updatedAt: "",
    });

    const fullName = computed(() => {
      const f = userData.value.firstname?.trim() || "";
      const l = userData.value.lastname?.trim() || "";
      return [f, l].filter(Boolean).join(" ");
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


    function onEditProfile() {
      window.location.href = "/profile/edit";
    }
    function onChangePassword() {
      window.location.href = "/profile/change-password";
    }

    const fetchApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/user/me`
        );
        userData.value = responseData as User;
        loading.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loading.value = false;
      }
    };

    onMounted(async () => {
      await fetchApi();
    });

    return {
      loading,
      errorMessage,
      imageBaseUrl,
      userData,
      fullName,
      onEditProfile,
      onChangePassword,
      fmtDateTime,
    };
  },
});
</script>
<template>
  <div class="py-4">
    <header class="mb-6 flex items-center justify-end">
      <div class="text-sm text-slate-500">Updated: {{ fmtDateTime(new Date()) }}</div>
    </header>
    <template v-if="!loading && !errorMessage">
      <section class="mb-6 grid gap-4 lg:grid-cols-2">
        <div class="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <div class="flex items-center gap-4">
            <img
              :src="
                userData?.avatarUrl
                  ? `${imageBaseUrl}${userData?.avatarUrl}`
                  : '/images/profile/profile.jpg'
              "
              alt="avatar"
              class="h-20 w-20 rounded-full object-cover ring-2 ring-slate-200"
            />
            <div>
              <h2 class="text-xl font-semibold text-slate-800">
                {{ fullName || userData.username }}
              </h2>
              <p class="text-sm text-slate-500">{{ userData.email }}</p>
              <p class="text-xs text-slate-400 mt-1">
                User Code: {{ userData.userCode }}
              </p>
            </div>
          </div>
          <div class="mt-4 flex flex-wrap gap-2">
            <button
              class="rounded-md bg-indigo-600 px-3 py-1.5 text-sm text-white hover:bg-indigo-700"
              @click="onEditProfile"
            >
              Edit Profile
            </button>
            <button
              class="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 hover:bg-slate-100"
              @click="onChangePassword"
            >
              Change Password
            </button>
          </div>
        </div>

        <div class="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          <h3 class="mb-3 text-base font-semibold text-slate-800">Account</h3>
          <dl class="space-y-2 text-sm">
            <div class="flex items-center justify-between">
              <dt class="text-slate-500">Username</dt>
              <dd class="text-slate-800 font-medium">{{ userData.username }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-slate-500">First Name</dt>
              <dd class="text-slate-800">{{ userData.firstname || "—" }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-slate-500">Last Name</dt>
              <dd class="text-slate-800">{{ userData.lastname || "—" }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-slate-500">Created At</dt>
              <dd class="text-slate-800">{{ fmtDateTime(userData.createdAt) }}</dd>
            </div>
            <div class="flex items-center justify-between">
              <dt class="text-slate-500">Updated At</dt>
              <dd class="text-slate-800">{{ fmtDateTime(userData.updatedAt) }}</dd>
            </div>
          </dl>
        </div>
      </section>
    </template>
    <div
      v-if="errorMessage"
      class="py-4 my-4 bg-red-50 border border-red-100 rounded-md text-center text-red-600"
    >
      {{ errorMessage }}
    </div>
  </div>
</template>
