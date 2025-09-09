import { ref } from "vue";

type Resolver = (v: boolean) => void;

const open = ref(false);
const text = ref("");
let resolver: Resolver | null = null;

export function useConfirm() {
  return (message: string): Promise<boolean> => {
    if (open.value && resolver) return Promise.resolve(false);

    text.value = message;
    open.value = true;
    return new Promise<boolean>((resolve) => {
      resolver = resolve;
    });
  };
}

export const confirmState = {
  open,
  text,
  resolveOk() {
    open.value = false;
    resolver?.(true);
    resolver = null;
  },
  resolveCancel() {
    open.value = false;
    resolver?.(false);
    resolver = null;
  },
};
