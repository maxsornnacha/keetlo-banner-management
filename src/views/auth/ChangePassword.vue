<script lang="ts">
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { Logout } from "@/utils/logout";
import { computed, defineComponent, reactive, ref } from "vue";

export default defineComponent({
  name: "ChangePasswordPage",
  setup() {
    const form = reactive({ current: "", newPw: "", confirm: "" });
    const show = reactive({ current: false, new: false, confirm: false });

    const errorMessage = ref<string | null>(null);
    const success = ref(false);
    const loading = ref(false);

    const hasMin = computed(() => form.newPw.length >= 8);
    const hasUpper = computed(() => /[A-Z]/.test(form.newPw));
    const hasLower = computed(() => /[a-z]/.test(form.newPw));
    const hasNumber = computed(() => /\d/.test(form.newPw));
    const hasSpecial = computed(() => /[^A-Za-z0-9]/.test(form.newPw));
    const notReuse = computed(
      () => !!form.newPw && form.newPw !== form.current
    );
    const matches = computed(() => form.newPw === form.confirm);

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
        notReuse.value &&
        matches.value &&
        !!form.current
    );

    function ruleClass(ok: boolean) {
      return ok ? "text-emerald-600" : "text-slate-500";
    }

    function onReset() {
      form.current = "";
      form.newPw = "";
      form.confirm = "";
      errorMessage.value = null;
      success.value = false;
    }

    async function onSubmit() {
      if (!canSubmit.value || loading.value) return;
      loading.value = true;
      errorMessage.value = null;
      success.value = false;
      try {
        const payload = {
          newPassword: form.newPw,
          password: form.current,
          confirmPassword: form.confirm,
        };
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/user/change-password`,
          "PUT",
          {},
          payload
        );
        success.value = true;
        onReset();
        Logout();
        window.location.reload();
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
      } finally {
        loading.value = false;
      }
    }
    return {
      errorMessage,
      success,
      onSubmit,
      show,
      form,
      strengthColorClass,
      strengthLabel,
      strengthBarClass,
      strengthPct,
      ruleClass,
      hasMin,
      hasUpper,
      hasLower,
      hasNumber,
      hasSpecial,
      matches,
      onReset,
      loading,
      canSubmit,
    };
  },
});
</script>

<template>
  <div class="flex justify-center">
    <div
      class="w-full lg:max-w-lg rounded-2xl bg-white lg:p-4 lg:shadow-sm lg:ring-1 lg:ring-slate-200"
    >
      <!-- Back -->
      <div class="mb-8">
        <router-link to="/profile">
          <button class="flex items-center gap-1 hover:text-indigo-700">
            <font-awesome-icon icon="chevron-left" class="w-7 h-7" />
            <p class="text-base">Back</p>
          </button>
        </router-link>
      </div>
      <h1 class="mb-1 text-xl font-semibold text-slate-800">Change Password</h1>
      <p class="mb-6 text-slate-500">Update your account password below.</p>

      <!-- Alerts -->
      <div
        v-if="errorMessage"
        class="mb-4 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-rose-700"
      >
        {{ errorMessage }}
      </div>
      <div
        v-if="success"
        class="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-emerald-700"
      >
        Password changed successfully.
      </div>

      <!-- Form -->
      <form @submit.prevent="onSubmit" class="space-y-4">
        <!-- Current -->
        <div>
          <label class="mb-1 block text-slate-600">Current Password</label>
          <div class="relative">
            <input
              :type="show.current ? 'text' : 'password'"
              v-model.trim="form.current"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              autocomplete="current-password"
              required
            />
            <button
              type="button"
              class="absolute inset-y-0 right-2 my-auto text-xs text-slate-500"
              @click="show.current = !show.current"
            >
              {{ show.current ? "Hide" : "Show" }}
            </button>
          </div>
        </div>

        <!-- New -->
        <div>
          <label class="mb-1 block text-slate-600">New Password</label>
          <div class="relative">
            <input
              :type="show.new ? 'text' : 'password'"
              v-model.trim="form.newPw"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              autocomplete="new-password"
              required
            />
            <button
              type="button"
              class="absolute inset-y-0 right-2 my-auto text-xs text-slate-500"
              @click="show.new = !show.new"
            >
              {{ show.new ? "Hide" : "Show" }}
            </button>
          </div>

          <!-- Strength -->
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

        <!-- Confirm -->
        <div>
          <label class="mb-1 block text-slate-600">Confirm New Password</label>
          <div class="relative">
            <input
              :type="show.confirm ? 'text' : 'password'"
              v-model.trim="form.confirm"
              class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
              autocomplete="new-password"
              required
            />
            <button
              type="button"
              class="absolute inset-y-0 right-2 my-auto text-xs text-slate-500"
              @click="show.confirm = !show.confirm"
            >
              {{ show.confirm ? "Hide" : "Show" }}
            </button>
          </div>
          <p v-if="form.confirm && !matches" class="mt-1 text-xs text-rose-600">
            Passwords do not match.
          </p>
        </div>

        <!-- Hints -->
        <div class="text-xs text-slate-500">
          • New password must be different from current password.<br />
          • Avoid using common or reused passwords.
        </div>

        <!-- Actions -->
        <div
          class="mt-2 flex flex-col lg:flex-row w-full items-center justify-end gap-2"
        >
          <button
            type="button"
            class="w-full rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-100"
            @click="onReset"
            :disabled="loading"
          >
            Reset
          </button>
          <button
            type="submit"
            class="w-full rounded-md bg-indigo-600 px-3 py-1.5 text-white hover:bg-indigo-700 disabled:opacity-60"
            :disabled="!canSubmit || loading"
          >
            <span v-if="loading">Updating…</span>
            <span v-else>Update Password</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
