<script lang="ts">
import Overallloader from "@/components/common/Overallloader.vue";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import { isFile } from "@/utils/image-check";
import { fileToBase64 } from "@/utils/image-handler";
import { useAlert } from "@/utils/use-alert";
import { useConfirm } from "@/utils/use-confirm";
import { defineComponent, onMounted, ref } from "vue";

export default defineComponent({
  name: "SystemSettingsPage",
  components: {
    Overallloader,
  },
  setup() {
    const confirm = useConfirm();
    const { openAlert, closeAlert } = useAlert();
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const original = ref({
      systemSettingId: null,
      productName: "",
      timezone: "",
      dateFormat: "",
      brandImageUrl: null,
      trackViews: false,
      trackClicks: false,
      trackActionLogs: false,
      publicKey: null,
      secretKey: null,
    });
    const settings = ref({
      systemSettingId: null,
      productName: "",
      timezone: "",
      dateFormat: "",
      brandImageUrl: null,
      trackViews: false,
      trackClicks: false,
      trackActionLogs: false,
      publicKey: null,
      secretKey: null,
    });
    const loading = ref(true);
    const loadingSubmit = ref(false);
    const loadingDefaultSubmit = ref(false);
    const errorMessage = ref<string | null>(null);

    const logoFile = ref<File | null>(null);
    const logoPreview = ref<string | null>(null);
    const MAX_SIZE_BYTES = 2 * 1024 * 1024;
    const ALLOWED_TYPES = [
      "image/png",
      "image/webp",
      "image/jpeg",
      "image/svg+xml",
    ];

    const fetchApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/setting`
        );
        const deep = JSON.parse(JSON.stringify({
          systemSettingId: responseData?.settings?.systemSettingId || "",
          productName: responseData?.settings?.productName || "",
          timezone: responseData?.settings?.timezone || "",
          dateFormat: responseData?.settings?.dateFormat || "",
          brandImageUrl: responseData?.settings?.brandImageUrl || null,
          trackViews:
            responseData?.settings?.trackViews &&
            responseData?.settings?.trackViews === 1
              ? true
              : false,
          trackClicks:
            responseData?.settings?.trackClicks &&
            responseData?.settings?.trackClicks === 1
              ? true
              : false,
          trackActionLogs:
            responseData?.settings?.trackActionLogs &&
            responseData?.settings?.trackActionLogs === 1
              ? true
              : false,
          publicKey: responseData?.settings?.publicKey || null,
          secretKey: responseData?.settings?.secretKey || null,
        }));
        settings.value = {...deep};
        original.value = {...deep};

        logoFile.value = null;
        logoPreview.value = null;
        loading.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loading.value = false;
      }
    };

    const dataHasChanged = () => {
      return !(JSON.stringify(settings.value) === JSON.stringify(original.value) && !logoFile.value)
    }

    onMounted(async () => {
      fetchApi();
    });

    const onLogoPicked = (e: Event) => {
      const input = e.target as HTMLInputElement;
      const file = input.files?.[0] || null;
      if (!file) return;

      if (!ALLOWED_TYPES.includes(file.type)) {
        openAlert(
          "Unsupported file type. Use PNG, JPG, WEBP, or SVG.",
          "error"
        );
        setTimeout(closeAlert, 1500);
        (e.target as HTMLInputElement).value = "";
        return;
      }
      if (file.size > MAX_SIZE_BYTES) {
        openAlert("File too large (max 2MB).", "warn");
        setTimeout(closeAlert, 1500);
        (e.target as HTMLInputElement).value = "";
        return;
      }

      logoFile.value = file;
      logoPreview.value = URL.createObjectURL(file);
    };

    const clearLogo = async () => {
      const status = await confirm("Clear current logo?");
      if (!status) return;
      settings.value.brandImageUrl = null;
      logoFile.value = null;

      if (logoPreview.value) URL.revokeObjectURL(logoPreview.value);
      logoPreview.value = null;
    };

    const generateSecretKey = async (status: string) => {
      let statusConfirm = true;
      if (status === "rotate") {
        statusConfirm = await confirm(
          "Would you like to retate the secret key ?"
        );
      }
      if (!statusConfirm) {
        return;
      }
      try {
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/setting/generate-secret-key`,
          "POST"
        );
        await fetchApi();
        openAlert("Secret key created successfully", "success");
        setTimeout(() => {
          closeAlert();
        }, 1500);
      } catch (error) {
        openAlert("Failed to create a secret key, please try again", "error");
        setTimeout(() => {
          closeAlert();
        }, 1500);
      }
    };

    const onSubmit = async () => {
      try {
        const status = await confirm(
          "Would you like to update the setting information ?"
        );
        if (!status) {
          return;
        }

        let base64Image: string | null = null;
        if (logoFile.value && isFile(logoFile.value)) {
          base64Image = await fileToBase64(logoFile.value);
        }
        loadingSubmit.value = true;

        const payload = {
          systemSettingId: settings.value.systemSettingId,
          productName: settings.value.productName,
          timezone: settings.value.timezone,
          dateFormat: settings.value.dateFormat,
          brandImageUrl: base64Image
            ? base64Image
            : settings.value.brandImageUrl || null,
          trackViews: settings.value.trackViews ? 1 : 0,
          trackClicks: settings.value.trackClicks ? 1 : 0,
          trackActionLogs: settings.value.trackActionLogs ? 1 : 0,
          publicKey: settings.value.publicKey,
          secretKey: settings.value.secretKey || null,
        };
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/setting/update`,
          "PUT",
          {},
          payload
        );
        loadingSubmit.value = false;
        openAlert("System Settings updated successfully !", "success");
        setTimeout(() => {
          window.location.reload();
          closeAlert();
        }, 1500);
      } catch (error) {
        loadingSubmit.value = false;
        openAlert(
          "Failed to update the setting information, please try again",
          "error"
        );
        setTimeout(() => {
          closeAlert();
        }, 1500);
      }
    };

    const resetToDefault = async () => {
      const status = await confirm(
        "Are you sure you would like to reset to the default settings ?"
      );
      if (!status) {
        return;
      }
      try {
        loadingDefaultSubmit.value = true;
        await fetchAPI(
          `${process.env.VUE_APP_API_URL}/setting/default`,
          "PUT",
          {},
          { confirmText: "DEFAULT" }
        );
        loadingDefaultSubmit.value = false;
        openAlert("Default settings updated successfully", "success");
        setTimeout(() => {
          window.location.reload();
          closeAlert();
        }, 1500);
      } catch (error) {
        loadingDefaultSubmit.value = false;
        openAlert(
          "Failed to update the setting information to default, please try again",
          "error"
        );
        setTimeout(() => {
          closeAlert();
        }, 1500);
      }
    };

    return {
      imageBaseUrl,
      settings,
      generateSecretKey,
      onSubmit,
      onLogoPicked,
      clearLogo,
      logoPreview,
      resetToDefault,
      loading,
      errorMessage,
      loadingSubmit,
      loadingDefaultSubmit,
      dataHasChanged,
    };
  },
});
</script>

<template>
    <div v-if="!loading && !errorMessage" class="py-4">
      <!-- Header -->
      <header class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-slate-800">System Settings</h1>
          <p class="text-sm text-slate-500">
            Configure global options for banner management.
          </p>
        </div>
      </header>

      <!-- Save bar (static demo – replace with your own JS to show/hide when dirty) -->
      <div
        class="mb-6 hidden items-center justify-between rounded-xl bg-amber-50 p-3 ring-1 ring-amber-200"
        id="savebar"
      >
        <span class="text-sm text-amber-800">You have unsaved changes.</span>
        <div class="space-x-2">
          <button type="button" class="btn-ghost">Discard</button>
          <button type="submit" class="btn-primary">Save All</button>
        </div>
      </div>

      <!-- SETTINGS FORM -->
      <form @submit.prevent="onSubmit" class="space-y-4">
        <!-- GENERAL -->
        <section class="card">
          <h2 class="mb-4 text-base font-semibold text-slate-800">General</h2>
          <div class="grid gap-4 sm:grid-cols-2">
            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Product Name</label
              >
              <input
                v-model="settings.productName"
                class="input"
                type="text"
                name="productName"
              />
            </div>
            <!-- <div>
              <label class="mb-1 block text-sm text-slate-600">Timezone</label>
              <select v-model="settings.timezone" class="input" name="timezone">
                <option>Asia/Bangkok</option>
                <option>UTC</option>
                <option>Asia/Tokyo</option>
              </select>
            </div> -->
            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Date Format</label
              >
              <select
                v-model="settings.dateFormat"
                class="input"
                name="dateFormat"
              >
                <option value="YYYY/MM/DD">YYYY/MM/DD</option>
                <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                <option value="MM/DD/YYYY">MM/DD/YYYY</option>
              </select>
            </div>
          </div>
        </section>

        <!-- BRANDING -->
        <section class="card">
          <h2 class="mb-4 text-base font-semibold text-slate-800">Branding</h2>
          <div class="flex items-start gap-4">
            <img
              :src="
                logoPreview
                  ? logoPreview
                  : settings.brandImageUrl
                  ? `${imageBaseUrl}${settings.brandImageUrl}`
                  : 'https://dummyimage.com/96x96/edeef2/6b7280&text=Logo'
              "
              class="h-16 w-16 rounded bg-slate-100 object-cover ring-1 ring-slate-200"
              alt="Logo preview"
            />
            <div class="space-y-2 text-sm">
              <div class="grid gap-2">
                <div class="flex gap-2">
                  <label class="btn-ghost cursor-pointer">
                    <input
                      type="file"
                      class="hidden"
                      accept="image/*"
                      @change="onLogoPicked"
                    />
                    Upload Logo
                  </label>
                  <button type="button" class="btn-ghost" @click="clearLogo">
                    Clear
                  </button>
                </div>
              </div>
              <p class="text-xs text-slate-500">
                PNG/SVG recommended & Maximum file size is 2 MB
              </p>
            </div>
          </div>
        </section>

        <!-- TRACKING / ANALYTICS -->
        <section class="card">
          <h2 class="mb-4 text-base font-semibold text-slate-800">
            Tracking & Analytics
          </h2>
          <div class="grid gap-4 sm:grid-cols-3">
            <!-- Toggles -->
            <div
              class="flex items-center justify-between border p-4 rounded-md"
            >
              <span class="text-sm text-slate-700">Track Views</span>
              <label
                class="relative inline-flex h-6 w-11 cursor-pointer items-center"
              >
                <input
                  v-model="settings.trackViews"
                  type="checkbox"
                  class="peer sr-only"
                  name="trackViews"
                  :checked="settings.trackViews"
                />
                <span
                  class="absolute h-6 w-11 rounded-full bg-slate-300 transition peer-checked:bg-indigo-600"
                ></span>
                <span
                  class="absolute ml-1 h-5 w-5 translate-x-0 rounded-full bg-white transition peer-checked:translate-x-5"
                ></span>
              </label>
            </div>

            <div
              class="flex items-center justify-between border p-4 rounded-md"
            >
              <span class="text-sm text-slate-700">Track Clicks</span>
              <label
                class="relative inline-flex h-6 w-11 cursor-pointer items-center"
              >
                <input
                  v-model="settings.trackClicks"
                  type="checkbox"
                  class="peer sr-only"
                  name="trackClicks"
                  :checked="settings.trackClicks"
                />
                <span
                  class="absolute h-6 w-11 rounded-full bg-slate-300 transition peer-checked:bg-indigo-600"
                ></span>
                <span
                  class="absolute ml-1 h-5 w-5 translate-x-0 rounded-full bg-white transition peer-checked:translate-x-5"
                ></span>
              </label>
            </div>

            <div
              class="flex items-center justify-between border p-4 rounded-md"
            >
              <span class="text-sm text-slate-700">Track Action Logs</span>
              <label
                class="relative inline-flex h-6 w-11 cursor-pointer items-center"
              >
                <input
                  v-model="settings.trackActionLogs"
                  type="checkbox"
                  class="peer sr-only"
                  name="trackActionLogs"
                  :checked="settings.trackActionLogs"
                />
                <span
                  class="absolute h-6 w-11 rounded-full bg-slate-300 transition peer-checked:bg-indigo-600"
                ></span>
                <span
                  class="absolute ml-1 h-5 w-5 translate-x-0 rounded-full bg-white transition peer-checked:translate-x-5"
                ></span>
              </label>
            </div>
          </div>
        </section>

        <!-- API -->
        <section class="card">
          <h2 class="mb-4 text-base font-semibold text-slate-800">API</h2>
          <div class="grid gap-3">
            <div>
              <label class="mb-1 block text-sm text-slate-600"
                >Public Key</label
              >
              <input
                disabled
                v-model="settings.publicKey"
                class="input font-mono"
                type="text"
                name="publicKey"
                placeholder="Public key"
              />
            </div>
            <!-- <div v-if="settings.secretKey">
              <label class="mb-1 block text-sm text-slate-600">Secret</label>
              <input
                disabled
                class="input font-mono"
                type="text"
                v-model="settings.secretKey"
                readonly
              />
            </div> -->
            <!-- <div v-if="!settings.secretKey">
              <label class="mb-1 block text-sm text-slate-600">Secret</label>
              <button
                @click="generateSecretKey('generate')"
                type="button"
                class="btn-ghost w-auto"
              >
                Generate secret key
              </button>
            </div>
            <div class="flex gap-2">
              <button
                @click="generateSecretKey('rotate')"
                v-if="settings.secretKey"
                type="button"
                class="btn-ghost"
              >
                Rotate Secret
              </button>
            </div> -->
          </div>
        </section>

        <!-- DANGER ZONE -->
        <section
          class="rounded-2xl border border-rose-200 bg-rose-50 p-6 shadow-sm lg:col-span-3"
        >
          <h2 class="mb-2 text-base font-semibold text-rose-800">
            Danger Zone
          </h2>
          <p class="mb-3 text-sm text-rose-700">
            Reset all system settings to defaults. This action cannot be undone.
          </p>
          <button
            @click="resetToDefault"
            type="button"
            class="btn bg-rose-600 text-white hover:bg-rose-700"
          >
            Reset to Defaults
          </button>
        </section>

        <!-- Footer actions -->
        <div class="lg:col-span-3 flex items-center justify-end gap-2">
          <button :disabled="!dataHasChanged()" type="submit" :class="[!dataHasChanged() ? 'btn-primary-disable':'btn-primary','w-full lg:w-auto']">
            Save Changes
          </button>
        </div>
      </form>
    </div>
      <div v-if="errorMessage" class="py-4 my-4 bg-red-50 border border-red-100 rounded-md text-center text-red-600">
        {{ errorMessage }}
    </div>
     <Overallloader :isOpen="loadingSubmit || loadingDefaultSubmit" />
</template>

<style>
/* tiny helpers */
.input {
  @apply w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 disabled:bg-slate-100;
}
.btn {
  @apply inline-flex items-center justify-center rounded-md px-3 py-1.5 text-sm font-medium;
}
.btn-ghost {
  @apply btn border border-slate-300 text-slate-700 hover:bg-slate-100;
}
.btn-primary {
  @apply btn bg-indigo-600 text-white hover:bg-indigo-700;
}
.btn-primary-disable {
    @apply btn bg-indigo-400 text-white;
}
.pill {
  @apply rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600;
}
.card {
  @apply rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200;
}
</style>
