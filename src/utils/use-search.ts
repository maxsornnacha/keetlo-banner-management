import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

export function useSearch(defaultSearch = '') {
  const route = useRoute();
  const router = useRouter();

  // Reactive search term (from URL or default)
  const searchTerm = ref((route.query.search as string) || defaultSearch);

  // Function to update the URL query
  const updateSearch = (value: string) => {
    searchTerm.value = value;

    // Merge current query with new search term
    const newQuery = { ...route.query, search: value || undefined }; // remove if empty
    router.push({ query: newQuery });
  };

  // React to URL query changes (back/forward buttons)
  watch(
    () => route.query.search,
    (newVal) => {
      searchTerm.value = (newVal as string) || '';
    }
  );

  return {
    searchTerm,
    updateSearch,
  };
}
