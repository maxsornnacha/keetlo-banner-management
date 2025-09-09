<script lang="ts">
import { Logout } from "@/utils/logout";
import { useConfirm } from "@/utils/use-confirm";
import { defineComponent, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

export default defineComponent({
  name: "SidebarComponent",
  components: {},
  setup() {
    const confirm = useConfirm();
    const navbarOpen = ref(false);
    const dropdownPopoverShow = ref(false);
    const btnDropdownRef = ref<HTMLElement | null>(null);
    const popoverDropdownRef = ref<HTMLElement | null>(null);
    const router = useRouter();
    const route = useRoute();
    const fullPath = ref(route.fullPath);
    const dropdownLists = ref({
      adminLayout: [
        {
          icon: "desktop",
          label: "Dashboard",
          path: "/",
        },
        {
          icon: "diagram-project",
          label: "Projects",
          path: "/projects",
        },
        {
          icon: "book",
          label: "Campaigns",
          path: "/campaigns",
        },
        {
          icon: "newspaper",
          label: "Banners",
          path: "/banners",
        },
      ],
      documentLayout: [
        {
          icon: "book",
          label: "Documentation",
          path: `/documentation`,
        },
        {
          icon: "code",
          label: "Embed Script",
          path: "/documentation#script",
        },
        {
          icon: "laptop-code",
          label: "Iframe Embed",
          path: "/documentation#iframe-embed",
        },
        {
          icon: "terminal",
          label: "API Usage",
          path: "/documentation#api-usage",
        },
      ],
      userLayout: [
        {
          icon: "diagnoses",
          label: "Action logs",
          path: "/action-logs",
        },
        {
          icon: "user",
          label: "Profile",
          path: "/profile",
        },
        {
          icon: "screwdriver-wrench",
          label: "Settings",
          path: "/settings",
        },
      ],
    });

    watch(
      () => route.fullPath,
      () => {
        fullPath.value = route.fullPath;
      }
    );

    // Toggle the Navbar
    const toggleNavbar = () => {
      navbarOpen.value = !navbarOpen.value;
    };

    // Toggle Dropdown
    const toggleDropdownPopover = () => {
      if (dropdownPopoverShow.value) {
        dropdownPopoverShow.value = false;
      } else {
        dropdownPopoverShow.value = true;
      }
    };

    // Close the dropdown if clicked outside
    const handleClickOutside = (e: MouseEvent) => {
      if (
        popoverDropdownRef.value &&
        !popoverDropdownRef.value.contains(e.target as Node) &&
        !btnDropdownRef.value?.contains(e.target as Node)
      ) {
        dropdownPopoverShow.value = false;
      }
    };

    // Attach event listener for detecting click outside the dropdown
    onMounted(() => {
      document.addEventListener("mousedown", handleClickOutside);
    });

    onBeforeUnmount(() => {
      document.removeEventListener("mousedown", handleClickOutside);
    });

    const handleLogout = async () => {
      const isConfirmed = await confirm(
        "Are you sure you would like to logout of the system?"
      );
      if (isConfirmed) {
        Logout();
      }
    };

    return {
      navbarOpen,
      toggleNavbar,
      dropdownPopoverShow,
      toggleDropdownPopover,
      btnDropdownRef,
      popoverDropdownRef,
      router,
      route,
      handleLogout,
      dropdownLists,
    };
  },
});
</script>

<template>
  <nav
    class="sticky top-0 w-full min-h-screen flex flex-wrap navbar-expand-lg bg-white shadow border-r"
  >
    <div class="px-4 container mx-auto flex flex-col flex-wrap">
      <!-- Logo Section -->
      <div
        class="w-full py-4 border-b border-slate-300 relative flex justify-between lg:w-auto lg:static lg:block lg:justify-start"
      >
        <router-link
          to="/"
          class="flex items-center gap-1 text-indigoGray-700 text-base font-bold leading-relaxed inline-block mr-4 py-2 whitespace-nowrap uppercase"
        >
          <img src="/logo.png" class="h-7 w-7 lg:h-10 lg:w-10 rounded-full" />
          <span class="text-sm lg:text-base">eetlo @ Banner Management</span>
        </router-link>
      </div>

      <!-- Navbar Links Section -->
      <div>
        <!-- Dropdown Menu Section -->
        <ul class="flex flex-col lg:flex-row list-none">
          <li class="flex items-center w-full">
            <div
              ref="popoverDropdownRef"
              class="text-base z-50 float-left py-2 list-none text-left rounded min-w-48 w-full"
            >
              <p class="py-2 text-sm text-slate-600">Admin Layout</p>
              <router-link
                v-for="list in dropdownLists.adminLayout"
                v-bind:key="list.label"
                :to="list.path"
                :class="[
                  'text-sm lg:text-base flex items-center gap-2 py-2 font-normal block w-full whitespace-nowrap bg-transparent',
                  (route.path.includes(list.path) && list.path !== '/') ||
                  (list.path === '/' && route.path === '/')
                    ? 'text-indigo-500'
                    : 'text-slate-500 hover:text-slate-400',
                ]"
              >
                <font-awesome-icon :icon="list.icon" class="w-5 h-5" />
                {{ list.label }}
              </router-link>
              <div class="h-0 my-2 border w-full border-solid"></div>
              <p class="py-2 text-sm text-slate-600">Document Layout</p>
              <div>
                <router-link
                  v-for="list in dropdownLists.documentLayout"
                  v-bind:key="list.label"
                  :to="list.path"
                  :class="[
                    'text-sm lg:text-base flex items-center gap-2 py-2 font-normal block w-full whitespace-nowrap bg-transparent',
                    (route.path+route.hash === list.path && list.path !== '/') ||
                    (list.path === '/' && route.path === '/')
                      ? 'text-indigo-500'
                      : 'text-slate-500 hover:text-slate-400',
                  ]"
                >
                  <font-awesome-icon :icon="list.icon" class="w-5 h-5" />
                  {{ list.label }}
                </router-link>
              </div>

              <div class="h-0 my-2 border w-full border-solid"></div>
              <p class="py-2 text-sm text-slate-600">User Layout</p>
              <router-link
                v-for="list in dropdownLists.userLayout"
                v-bind:key="list.label"
                :to="list.path"
                :class="[
                  'text-sm lg:text-base flex items-center gap-2 py-2 font-normal block w-full whitespace-nowrap bg-transparent',
                  route.path.includes(list.path)
                    ? 'text-indigo-500'
                    : 'text-slate-500 hover:text-slate-400',
                ]"
              >
                <font-awesome-icon :icon="list.icon" class="w-5 h-5" />
                {{ list.label }}
              </router-link>
              <button
                @click="handleLogout"
                class="flex items-center gap-2 py-2 font-normal block w-full whitespace-nowrap bg-transparent text-slate-500 hover:text-slate-400 hover:!bg-transparent"
              >
                <font-awesome-icon icon="right-from-bracket" class="w-5 h-5" />
                Logout
              </button>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<style scoped>
/* Optional styling */
button {
  transition: background-color 0.2s ease;
}

button:hover {
  background-color: #4c51bf;
}
</style>
