import { toErrorMessage } from "@/utils/fetch-api";
import { getLocalStorage, removeLocalStorage } from "@/utils/local-storage";
import { defineStore } from "pinia";

type User = {
  username: string;
  email: string;
  firstname: string;
  lastname: string;
  avatarUrl: string | null;
  timezone: string | null;
  dateFormat: string | null;
  productName: string | null;

};

type ApiResponse<T> = { data: T; message: string; status: string };
type Result<T> = { ok: true; data: T } | { ok: false; error: string };

export const useUserStore = defineStore("user", {
  state: () => ({
    user: null as User | null,
    isAuthenticated: false,
  }),
  actions: {
    async initializeUser() {
      const token = getLocalStorage("token");
      if (!token) {
        this.user = null;
        this.isAuthenticated = false;
        return { ok: false, error: "No token" } as Result<null>;
      }

      const res = await this.fetchUser(token);

      if (!res.ok) {
        this.logout(); 
      }
      return res;
    },
    async fetchUser(token: string): Promise<Result<User>> {
      try {
        const response = await fetch(`${process.env.VUE_APP_API_URL}/user`, {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          let msg = `Request failed (${response.status})`;
          try {
            const j = (await response.json()) as Partial<ApiResponse<unknown>>;
            if (j?.message) msg = j.message;
          } catch {
            /* non-JSON error body */
          }
          return { ok: false, error: msg };
        }

        const json = (await response.json()) as ApiResponse<User>;
        this.user = json.data;
        this.isAuthenticated = true;
        return { ok: true, data: json.data };
      } catch (error: unknown) {
        const msg = toErrorMessage(error);
        return { ok: false, error: msg };
      }
    },
    logout() {
      removeLocalStorage("token");
      this.user = null;
      this.isAuthenticated = false;
    },
  },
});
