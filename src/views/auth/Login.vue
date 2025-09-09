<script lang="ts">
import Button from "@/components/ui/Button.vue";
import Loader from "@/components/ui/Loader.vue";
import { useUserStore } from "@/store";
import { Body } from "@/types/Login";
import { fetchAPI } from "@/utils/fetch-api";
import { setLocalStorage } from "@/utils/local-storage";
import { useAlert } from "@/utils/use-alert";
import { defineComponent, ref } from "vue";
import { useRouter } from "vue-router";

export default defineComponent({
  name: "LoginPage",
  components: {
    Button,
    Loader,
  },
  setup() {
    const userStore = useUserStore();
    userStore.initializeUser();
    const router = useRouter();
    const username = ref("");
    const password = ref("");
    const errorMessage = ref("");
    const showPassword = ref(false);
    const { openAlert, closeAlert } = useAlert();
    const loadingSubmit = ref(false);

    const handleLogin = async () => {
      if (!username.value || !password.value) {
        errorMessage.value = "Please enter your username and password";
        return;
      }

      const body: Body = {
        username: username.value,
        password: password.value,
      };
      loadingSubmit.value = true;

      await fetchAPI(
        `${process.env.VUE_APP_API_URL}/user/login`,
        "POST",
        {},
        body
      )
        .then(async (token) => {
          setLocalStorage("token", token);
          userStore.initializeUser();
          openAlert("Login successfully!", "success");
          setTimeout(() => {
            router.push("/");
            closeAlert();
          }, 1500);
          errorMessage.value = "";
        })
        .catch((error) => {
          errorMessage.value = error;
          loadingSubmit.value = false;
        });
    };

    return {
      username,
      password,
      errorMessage,
      handleLogin,
      showPassword,
      loadingSubmit,
    };
  },
});
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center">
    <div class="w-full lg:max-w-md lg:p-8 bg-white rounded-lg lg:shadow-lg">
      <h2 class="text-2xl font-semibold text-gray-800 text-center mb-6">
        Login
      </h2>
      <form @submit.prevent="handleLogin">
        <div class="mb-4 space-y-1">
          <label for="username" class="block text-sm font-medium text-gray-700"
            >Username</label
          >
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="Please enter your username"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
        </div>

        <div class="mb-6 space-y-1">
          <label for="password" class="block text-sm font-medium text-gray-700"
            >Password</label
          >
          <div class="relative">
            <input
              id="password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="Please enter your password"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              required
            />
            <button
              type="button"
              class="absolute inset-y-0 right-2 my-auto text-xs text-slate-500"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? "Hide" : "Show" }}
            </button>
          </div>
        </div>

        <p v-if="errorMessage" class="text-red-500 text-sm text-start mb-4">
          {{ errorMessage }}
        </p>

        <div class="flex items-center justify-between">
          <Button
            v-if="!loadingSubmit"
            type="submit"
            class="w-full bg-indigo-600 text-white font-semibold rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            Login
          </Button>
          <Button
            v-if="loadingSubmit"
            type="submit"
            disabled
            class="w-full text-white font-semibold rounded-md bg-indigo-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 flex justify-center items-center gap-2"
          >
            <Loader customClass="fill-indigo-400 text-white w-5 h-5" />
            Loading
          </Button>
        </div>
      </form>

      <div class="mt-6 text-center">
        <p class="text-sm text-gray-600">
          Already have an account?
          <a href="/register" class="text-indigo-600 hover:text-indigo-800"
            >Register</a
          >
        </p>
      </div>
    </div>
  </div>
</template>
