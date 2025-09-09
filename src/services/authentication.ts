import { getLocalStorage } from "@/utils/local-storage";

export const JWTAuthentication = () => {
  try {
    const token = getLocalStorage("token");
    if(!token){
        return null;
    }
    return `Bearer ${token}`;
  } catch (error) {
    return null;
  }
};
