import { removeLocalStorage } from "./local-storage";
import { useAlert } from "./use-alert";

export const Logout = () => {
            const { openAlert } = useAlert();
            removeLocalStorage("token");
            openAlert("Logout successfully!", "success");
            setTimeout(() => {
               window.location.reload();
            }, 1500);
    
}