
<script lang="ts">
import { defineComponent, onMounted, ref, watch } from 'vue';
import Header from './components/common/Header.vue';
import { useRoute } from 'vue-router';
import Sidebar from './components/common/Sidebar.vue';
import Confirm from './components/ui/Confirm.vue';
import Alert from './components/ui/Alert.vue';
import { useAlert } from './utils/use-alert';

export default defineComponent({
  name: 'App',
  components: {
    Header,
    Sidebar,
    Confirm,
    Alert,
  },
  setup(){
    const route = useRoute();
    const showHeader = ref(true);
    const alertInstance = ref<InstanceType<typeof Alert> | null>(null);
    const { setAlertInstance } = useAlert();

    onMounted(() => {
      if (alertInstance.value) {
        setAlertInstance(alertInstance.value);
      }
    });

    watch(() => route.path, async (newPath) => {
      showHeader.value = !(
        newPath === '/login' 
      || newPath === '/register' 
      || newPath.includes('/projects/create')
      || newPath.includes('/projects/edit')
      || newPath.includes('/campaigns/create')
      || newPath.includes('/campaigns/edit')
      || newPath.includes('/banners/create')
       || newPath.includes('/banners/edit')
      || newPath.includes('/profile/change-password')
      || newPath.includes('/profile/edit')
       || newPath.includes('/coming-soon')
    );
    }, { immediate: true }); 

    return {
      showHeader,
      alertInstance,
    };
  }
});
</script>
<template>
  <div class="app" id="alert-container">
    <div class="grid grid-cols-12">
      <div class="lg:col-span-3 hidden lg:block" v-if="showHeader">
        <Sidebar/>
      </div>
      <div :class="[showHeader ? 'col-span-12 lg:col-span-9' : 'col-span-12', 'relative']">
       <Header v-if="showHeader"/>
       <div :class="['px-4 pb-8 relative',showHeader ? 'bg-white':'bg-white pt-8']">
        <router-view/>
       </div>
      </div>
    </div>
    <Confirm/>
    <Alert ref="alertInstance" />
  </div>
</template>
