import { ref } from "vue";
import { createApp } from "vue";
import Alert from "@/components/ui/Alert.vue";

// Create a ref to hold the alert component instance
const alertInstance = ref<InstanceType<typeof Alert> | null>(null);

export function useAlert() {
  return {
    openAlert: (message: string, type: "success" | "error" | "warn" | "info" = "info") => {
      if (alertInstance.value) {
        alertInstance.value.openAlert(message, type);
      }
    },
   closeAlert: () => {
      if (alertInstance.value) {
        alertInstance.value.closeAlert();
      }
    },
    setAlertInstance: (instance: InstanceType<typeof Alert>) => {
      alertInstance.value = instance;
    },
  };
}

// Register globally
const app = createApp(Alert);
app.mount("#alert-container");
