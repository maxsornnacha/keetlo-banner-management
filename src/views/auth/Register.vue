<script lang="ts">
import Button from "@/components/ui/Button.vue";
import Loader from "@/components/ui/Loader.vue";
import { Body } from "@/types/Register";
import { fetchAPI } from "@/utils/fetch-api";
import { useAlert } from "@/utils/use-alert";
import { computed, defineComponent, ref } from "vue";
import { useRouter } from "vue-router";

export default defineComponent({
  name: "RegisterPage",
  components: {
    Button,
    Loader,
  },
  setup() {
    const router = useRouter();
    const firstname = ref<string>("");
    const lastname = ref<string>("");
    const username = ref<string>("");
    const email = ref<string>("");
    const password = ref<string>("");
    const confirmPassword = ref<string>("");
    const errorMessage = ref<string>("");
    const hasMin = computed(() => password.value.length >= 8);
    const hasUpper = computed(() => /[A-Z]/.test(password.value));
    const hasLower = computed(() => /[a-z]/.test(password.value));
    const hasNumber = computed(() => /\d/.test(password.value));
    const hasSpecial = computed(() => /[^A-Za-z0-9]/.test(password.value));
    const matches = computed(() => password.value === confirmPassword.value);
    const { openAlert, closeAlert } = useAlert();
    const loadingSubmit = ref(false);

    // strength score (0..5)
    const score = computed(() =>
      [
        hasMin.value,
        hasUpper.value,
        hasLower.value,
        hasNumber.value,
        hasSpecial.value,
      ].reduce((s, ok) => s + (ok ? 1 : 0), 0)
    );
    const strengthPct = computed(() => (score.value / 5) * 100);
    const strengthLabel = computed(() => {
      if (score.value <= 2) return "Weak";
      if (score.value === 3) return "Fair";
      if (score.value === 4) return "Good";
      return "Strong";
    });
    const strengthBarClass = computed(() => ({
      "bg-rose-500": score.value <= 2,
      "bg-amber-500": score.value === 3,
      "bg-emerald-500": score.value >= 4,
    }));
    const strengthColorClass = computed(() => ({
      "text-rose-600": score.value <= 2,
      "text-amber-600": score.value === 3,
      "text-emerald-600": score.value >= 4,
    }));

    const canSubmit = computed(
      () =>
        hasMin.value &&
        hasUpper.value &&
        hasLower.value &&
        hasNumber.value &&
        hasSpecial.value &&
        matches.value &&
        email.value &&
        username.value &&
        firstname.value &&
        lastname.value
    );
    function ruleClass(ok: boolean) {
      return ok ? "text-emerald-600" : "text-slate-500";
    }

    const handleRegister = async () => {
      if (!canSubmit.value) {
        return;
      }
      if (
        !firstname.value ||
        !lastname.value ||
        !username.value ||
        !email.value ||
        !password.value ||
        !confirmPassword.value
      ) {
        errorMessage.value = "Please fill in all fields";
        return;
      }

      const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
      if (!emailRegex.test(email.value)) {
        errorMessage.value = "Please enter a valid email";
        return;
      }

      if (password.value.length < 6) {
        errorMessage.value = "Password must be at least 6 characters long";
        return;
      }

      if (confirmPassword.value !== password.value) {
        errorMessage.value = "Passwords do not match";
        return;
      }
      loadingSubmit.value = true;

      const body: Body = {
        firstname: firstname.value,
        lastname: lastname.value,
        username: username.value,
        email: email.value,
        password: password.value,
        confirmPassword: confirmPassword.value,
      };

      await fetchAPI(
        `${process.env.VUE_APP_API_URL}/user/register`,
        "POST",
        {},
        body
      )
        .then(async (response) => {
          openAlert("Register successfully", "success");
          setTimeout(() => {
            router.push("/login");
            closeAlert();
          }, 1500);
          errorMessage.value = "";
        })
        .catch((error) => {
          loadingSubmit.value = false;
          errorMessage.value = error?.message;
        });
    };

    return {
      firstname,
      lastname,
      username,
      email,
      password,
      confirmPassword,
      errorMessage,
      handleRegister,
      strengthPct,
      strengthLabel,
      strengthBarClass,
      strengthColorClass,
      canSubmit,
      ruleClass,
      hasMin,
      hasUpper,
      hasLower,
      hasNumber,
      hasSpecial,
      matches,
      loadingSubmit,
    };
  },
});
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center">
    <div class="w-full lg:max-w-lg lg:p-8 bg-white rounded-lg lg:shadow-lg">
      <h2 class="text-2xl font-semibold text-gray-800 text-center mb-6">
        Register
      </h2>
      <form @submit.prevent="handleRegister">
        <div class="mb-4 space-y-1">
          <label for="firstname" class="block text-sm font-medium text-gray-700"
            >Firstname <span class="text-indigo-500">*</span></label
          >
          <input
            id="firstname"
            v-model="firstname"
            type="text"
            placeholder="Please enter your firstname"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
        </div>

        <div class="mb-4 space-y-1">
          <label for="lastname" class="block text-sm font-medium text-gray-700"
            >Lastname <span class="text-indigo-500">*</span></label
          >
          <input
            id="lastname"
            v-model="lastname"
            type="text"
            placeholder="Please enter your lastname"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
        </div>

        <div class="mb-4 space-y-1">
          <label for="username" class="block text-sm font-medium text-gray-700"
            >Username <span class="text-indigo-500">*</span></label
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

        <div class="mb-4 space-y-1">
          <label for="email" class="block text-sm font-medium text-gray-700"
            >Email <span class="text-indigo-500">*</span></label
          >
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="Please enter your email"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
        </div>

        <div class="mb-6 space-y-1">
          <label for="password" class="block text-sm font-medium text-gray-700"
            >Password <span class="text-indigo-500">*</span></label
          >
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="Please enter your password"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
          <div class="mt-2">
            <div
              class="mb-1 flex items-center justify-between text-xs text-slate-500"
            >
              <span>Password strength</span>
              <span :class="strengthColorClass">{{ strengthLabel }}</span>
            </div>
            <div class="h-2 w-full overflow-hidden rounded bg-slate-100">
              <div
                class="h-full transition-all"
                :class="strengthBarClass"
                :style="{ width: strengthPct + '%' }"
              ></div>
            </div>
            <ul class="mt-2 list-disc pl-5 text-xs text-slate-500 space-y-1">
              <li :class="ruleClass(hasMin)">At least 8 characters</li>
              <li :class="ruleClass(hasUpper)">Contains uppercase letter</li>
              <li :class="ruleClass(hasLower)">Contains lowercase letter</li>
              <li :class="ruleClass(hasNumber)">Contains a number</li>
              <li :class="ruleClass(hasSpecial)">
                Contains a special character
              </li>
            </ul>
          </div>
        </div>

        <div class="mb-6 space-y-1">
          <label
            for="confirmPassword"
            class="block text-sm font-medium text-gray-700"
            >Confirm Password <span class="text-indigo-500">*</span></label
          >
          <input
            id="confirmPassword"
            v-model="confirmPassword"
            type="password"
            placeholder="Please confirm your password"
            class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
          <p
            v-if="confirmPassword && !matches"
            class="mt-1 text-xs text-rose-600"
          >
            Passwords do not match.
          </p>
        </div>
        <p v-if="errorMessage" class="text-red-500 text-sm mb-4">
          {{ errorMessage }}
        </p>

        <div class="flex items-center justify-between">
          <Button
            v-if="!loadingSubmit"
            :disabled="!canSubmit"
            type="submit"
            :class="[
              'w-full text-white font-semibold rounded-md focus:outline-none focus:ring-2',
              canSubmit
                ? 'bg-indigo-600 hover:bg-indigo-700 focus:ring-indigo-500'
                : 'bg-indigo-400',
            ]"
          >
            Register
          </Button>
          <Button
            v-if="loadingSubmit"
            disabled
            class="w-full text-white font-semibold rounded-md focus:outline-none focus:ring-2 bg-indigo-400 flex justify-center items-center gap-2"
          >
            <Loader customClass="fill-indigo-400 text-white w-5 h-5" />
            Loading
          </Button>
        </div>
      </form>

      <div class="mt-6 text-center">
        <p class="text-sm text-gray-600">
          Already have an account?
          <a href="/login" class="text-indigo-600 hover:text-indigo-800"
            >Login</a
          >
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
button {
  transition: background-color 0.2s;
}
</style>
