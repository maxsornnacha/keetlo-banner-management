<script>
import { defineComponent, onMounted, ref, watch } from "vue";
import { useUserStore } from "@/store";
import { useRoute } from "vue-router";
import Drawer from "../ui/Drawer.vue";
import Sidebar from "./Sidebar.vue";

export default defineComponent({
  name: "HeaderComponent",
  components: {
    Drawer,
    Sidebar,
  },
  setup() {
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const userStore = useUserStore();
    const user = ref(null);
    const openProfileMenu = ref(false);
    const pagename = ref("Dashboard");
    const route = useRoute();
    const openDrawer = ref(false);

    watch(()=> route.fullPath,
    ()=>{
      openDrawer.value = false;
    }
  )

    watch(
      () => route.path,
      (pathname) => {
        if (pathname === "/") {
          pagename.value = "Dashboard";
        } else if (pathname.includes("/projects")) {
          pagename.value = "Projects";
        } else if (pathname.includes("/campaigns")) {
          pagename.value = "Campaigns";
        } else if (pathname.includes("/profile")) {
          pagename.value = "Profile";
        } else if (pathname.includes("/settings")) {
          pagename.value = "Settings";
        } else if (pathname.includes("/banners")) {
          pagename.value = "Banners";
        } else if (pathname.includes("/action-logs")) {
          pagename.value = "Action logs";
        } else if (pathname.includes("/documentation")) {
          pagename.value = "Documentation";
        }
      }
    );

    onMounted(async () => {
      await userStore.initializeUser();

      if (userStore.user) {
        user.value = userStore.user;
      }
    });

    const handleOpenDrawer = () => {
      openDrawer.value = !openDrawer.value;
    }

    return {
      user,
      openProfileMenu,
      pagename,
      imageBaseUrl,
      openDrawer,
      handleOpenDrawer,
    };
  },
});
</script>

<template>
  <div
    class="header w-full sticky lg:static top-0 z-20 bg-white border-b border-slate-100"
  >
    <div class="relative p-4 flex gap-4 items-center justify-between">
      <div class="flex items-center gap-2">
        <button @click="handleOpenDrawer">
        <font-awesome-icon icon="bars" class="w-6 h-6 lg:hidden hover:text-indigo-600 cursor-pointer"/>
       </button>
        <p class="text-lg font-semibold">{{ pagename }}</p>
      </div>
      <div class="flex items-center gap-2">
        <div v-if="user?.brandImageUrl">
             <img
              :src="`${imageBaseUrl}${user?.brandImageUrl}`"
              class="object-cover w-[35px] h-[35px] rounded-md border border-slate-100"
              alt="Profile image"
            />
        </div>
        <div v-if="user">
          <router-link
          to="/profile" 
          class="flex items-center gap-2">
            <img
              :src="
                user?.avatarUrl
                  ? `${imageBaseUrl}${user?.avatarUrl}`
                  : '/images/profile/profile.jpg'
              "
              class="object-cover w-[35px] h-[35px] rounded-full"
              alt="Profile image"
            />
            <div class="hidden lg:block">
              <p>{{ user?.firstname }} {{ user?.lastname }}</p>
              <p class="text-slate-400 text-xs">
                Username: {{ user?.username }}
              </p>
            </div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
     <Drawer
    topic="Menu"
    v-model="openDrawer"
    side="left"          
    width="400px"           
    height="70vh"       
    :closeOnEsc="true"
    :closeOnBackdrop="true"
    :zIndex="30"
    aria-label="Campaign filters"
  >
    <Sidebar/>
  </Drawer>
</template>

