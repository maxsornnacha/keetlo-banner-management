<script>
import { defineComponent } from "vue";
import { useRoute } from "vue-router";

export default defineComponent({
  name: "ComingSoonPage",
  setup() {
    const route = useRoute();
    const back = route?.query?.back || "/";
    return { back };
  },
});
</script>

<template>
  <div class="min-h-[70vh] flex items-center justify-center">
    <div class="text-center bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
      <h1 class="text-3xl font-bold text-indigo-600 mb-4">
        We’re Launching Soon!
      </h1>
      <p class="text-lg text-gray-600 mb-6">
        Our website is coming soon. Stay tuned!
      </p>

      <p class="text-gray-500 mb-6">Thank you for your patience.</p>

      <router-link
        :to="back"
        class="border border-slate-200 py-2 px-6 rounded-md hover:bg-gray-200"
        >Back</router-link
      >
    </div>
  </div>
</template>
<style scoped>
.countdown {
  font-size: 2rem;
  font-weight: bold;
  color: #4b5563;
}
</style>
