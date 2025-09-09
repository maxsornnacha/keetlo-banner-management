<script lang="ts">
import { ref, computed, defineComponent, onMounted } from "vue";
import type { Banner } from "@/types/Banner";
import type { Campaign } from "@/types/Campaign";
import type { Project } from "@/types/Project";
import Card from "@/components/ui/Card.vue";
import KpiCard from "@/components/ui/KpiCard.vue";
import StatusPill from "@/components/ui/StatusPill.vue";
import { fetchAPI, toErrorMessage } from "@/utils/fetch-api";
import MiniBar from "@/components/ui/Minibar.vue";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/store";
import { formatDate, formatDateTime } from "@/utils/date-handler";

export default defineComponent({
  name: "DashboardPage",
  components: {
    Card,
    KpiCard,
    StatusPill,
    MiniBar,
  },
  setup() {
    const imageBaseUrl = process.env.VUE_APP_API_URL;
    const projects = ref<Project[]>([]);
    const campaigns = ref<Campaign[]>([]);
    const banners = ref<Banner[]>([]);
    const loading = ref(true);
    const errorMessage = ref<string | null>(null);
    const skeletonRows = Array.from({ length: 6 }, (_, i) => i);
    const userStore = useUserStore()
    const { user } = storeToRefs(userStore);
    const tz = computed(() => user.value?.timezone ?? 'UTC');
    const df = computed(() => user.value?.dateFormat ?? 'YYYY-MM-DD');

    const fmtDateTime = (
    input: string | number | Date | null | undefined,
    ): string => {
    return formatDateTime(input, {
      timezone: tz.value,
      dateFormat: df.value,
    });
    }

    const fmtDate = (
    input: string | number | Date | null | undefined,
    ): string => {
    return formatDate(input, {
      timezone: tz.value,
      dateFormat: df.value,
    });
    }

    const fetchApi = async () => {
      try {
        const responseData = await fetchAPI(
          `${process.env.VUE_APP_API_URL}/dashboard`
        );
        projects.value = responseData.projects as Project[];
        campaigns.value = responseData.campaigns as Campaign[];
        banners.value = responseData.banners as Banner[];
        loading.value = false;
      } catch (error: unknown) {
        errorMessage.value = toErrorMessage(error);
        loading.value = false;
      }
    };

    onMounted(async () => {
      await fetchApi();
    });

    const now = new Date().toLocaleString();

    // ----- Indices -----
    const campaignIndex = computed<Record<string, Campaign>>(() => {
      const m: Record<string, Campaign> = {};
      campaigns.value.forEach((c) => (m[c.campaignId] = c));
      return m;
    });

    const projectNameIndex = computed<Record<string, string>>(() => {
      const m: Record<string, string> = {};
      projects.value.forEach((p) => (m[p.projectId] = p.projectName));
      return m;
    });

    const campaignBannerCounts = computed<Record<string, number>>(() => {
      const m: Record<string, number> = {};
      banners.value.forEach((b) => {
        m[b.campaignId] = (m[b.campaignId] ?? 0) + 1;
      });
      return m;
    });

    // ----- Filters / aggregations -----
    const activeCampaigns = computed(() =>
      campaigns.value.filter((c) => c.status === "active")
    );

    const totals = computed(() => {
      const views = banners.value.reduce((s, b) => s + (b.views || 0), 0);
      const clicks = banners.value.reduce((s, b) => s + (b.clicks || 0), 0);
      return { views, clicks };
    });

    function ctr(b: Banner): number {
      const v = b.views || 0;
      return v === 0 ? 0 : ((b.clicks || 0) / v) * 100; // percentage
    }

    const kpis = computed(() => {
      const totalProjects = projects.value.length;
      const activeCampaignsCount = activeCampaigns.value.length;
      const totalBanners = banners.value.length;
      const avgCtr =
        totalBanners === 0
          ? 0
          : banners.value.reduce((s, b) => s + ctr(b), 0) / totalBanners; // already %
      return {
        totalProjects,
        activeCampaigns: activeCampaignsCount,
        totalBanners,
        avgCtr,
      };
    });

    const topBannersByClicks = computed(() =>
      [...banners.value]
        .sort((a, b) => (b.clicks || 0) - (a.clicks || 0))
        .slice(0, 5)
    );

    const bannersByProject = computed<Record<string, number>>(() => {
      const m: Record<string, number> = {};
      banners.value.forEach((b) => {
        const projectId = campaignIndex.value[b.campaignId]?.projectId;
        if (!projectId) return;
        m[projectId] = (m[projectId] ?? 0) + 1;
      });
      return m;
    });

    function pctOfTotalBanners(projectId: string): number {
      const total = banners.value.length || 1;
      return Math.round(
        ((bannersByProject.value[projectId] ?? 0) / total) * 100
      );
    }

    function formatPct(n: number): string {
      return (Math.round(n * 10) / 10).toFixed(1);
    }

    return {
      projects,
      campaigns,
      banners,
      now,
      projectNameIndex,
      campaignBannerCounts,
      totals,
      kpis,
      topBannersByClicks,
      pctOfTotalBanners,
      formatPct,
      campaignIndex,
      activeCampaigns,
      bannersByProject,
      ctr,
      imageBaseUrl,
      loading,
      skeletonRows,
      errorMessage,
      fmtDateTime,
      fmtDate,
    };
  },
});
</script>

<template>
  <head>
     <title>Max</title>
  </head>
  <div v-if="!loading && !errorMessage" class="py-4">
    <header class="mb-6 flex items-center justify-end">
      <div class="text-sm text-slate-500">Updated: {{ fmtDateTime(new Date()) }}</div>
    </header>

    <!-- KPI cards -->
    <section class="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-6">
      <KpiCard label="Projects" :value="kpis.totalProjects" />
      <KpiCard label="Campaigns (active)" :value="kpis.activeCampaigns" />
      <KpiCard label="Total Banners" :value="kpis.totalBanners" />
      <KpiCard label="Avg. CTR" :value="formatPct(kpis.avgCtr)" suffix="%" />
    </section>

    <!-- Overview row -->
    <section class="grid gap-4 lg:grid-cols-3 mb-6">
      <!-- Active campaigns -->
      <div class="lg:col-span-2">
        <Card>
          <template #title>Active Campaigns</template>
          <template #body>
            <div class="overflow-x-auto max-h-[400px] overflow-y-auto w-full">
              <table class="min-w-full text-sm">
                <thead>
                  <tr class="text-left text-slate-500">
                    <th class="py-2 pr-3">Campaign</th>
                    <th class="py-2 pr-3">Project</th>
                    <th class="py-2 pr-3">Status</th>
                    <th class="py-2 pr-3">Start</th>
                    <th class="py-2 pr-3">End</th>
                    <th class="py-2 pr-3 text-right">Banners</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="c in activeCampaigns"
                    :key="c.campaignId"
                    class="border-t"
                  >
                    <td class="py-2 pr-3 font-medium text-slate-800">
                      {{ c.campaignName }}
                    </td>
                    <td class="py-2 pr-3">
                      {{ projectNameIndex[c.projectId] ?? "—" }}
                    </td>
                    <td class="py-2 pr-3">
                      <StatusPill :status="c.status" />
                    </td>
                    <td class="py-2 pr-3">{{ fmtDate(c.startDate) }}</td>
                    <td class="py-2 pr-3">
                      {{ c.endDate ? fmtDate(c.endDate) : "—" }}
                    </td>
                    <td class="py-2 pr-3 text-right">
                      {{ campaignBannerCounts[c.campaignId] ?? 0 }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </Card>
      </div>

      <!-- Top banners by clicks -->
      <Card>
        <template #title>Top Banners (by clicks)</template>
        <template #body>
          <ol class="space-y-3">
            <li
              :v-if="topBannersByClicks.length > 0"
              v-for="b in topBannersByClicks"
              :key="b.bannerId"
              class="flex items-center justify-between"
            >
              <div class="min-w-0">
                <p class="truncate font-medium text-slate-800">
                  {{
                    projectNameIndex[campaignIndex[b.campaignId]?.projectId] ??
                    "—"
                  }}
                </p>
                <p class="truncate text-xs text-slate-500">
                  Campaign:
                  {{ campaignIndex[b.campaignId]?.campaignName ?? "—" }}
                </p>
                <p class="text-xs text-slate-400">
                  Size: {{ b.imageWidth ?? "?" }}×{{ b.imageHeight ?? "?" }}
                </p>
              </div>
              <div class="text-right">
                <div class="font-semibold">
                  {{ b.clicks?.toLocaleString?.() ?? b.clicks }} clicks
                </div>
                <div class="text-xs text-slate-500">
                  {{ formatPct(ctr(b)) }}% CTR
                </div>
              </div>
            </li>
            <li v-if="topBannersByClicks.length === 0">
              <p class="text-slate-500 text-center">
                No Banner Found Yet 
              </p>
            </li>
          </ol>
        </template>
      </Card>
    </section>

    <!-- Traffic and distribution -->
    <section class="grid gap-4 lg:grid-cols-3">
      <Card class="lg:col-span-2">
        <template #title>Views &amp; Clicks (sum)</template>
        <template #body>
          <div class="grid gap-4 grid-cols-1">
            <MiniBar
              label="Views"
              :value="totals.views"
              :max="Math.max(totals.views, totals.clicks) || 1"
            />
            <MiniBar
              label="Clicks"
              :value="totals.clicks"
              color="bg-emerald-500"
              :max="Math.max(totals.views, totals.clicks) || 1"
            />
          </div>
        </template>
      </Card>

      <!-- Distribution by project -->
      <Card>
        <template #title>Banner Distribution by Project</template>
        <template #body>
          <ul class="space-y-3">
            <li v-for="p in projects" :key="p.projectId" class="space-y-1">
              <div class="flex items-center justify-between text-sm">
                <span class="truncate">{{ p.projectName }}</span>
                <span class="tabular-nums text-slate-600">{{
                  bannersByProject[p.projectId] ?? 0
                }}</span>
              </div>
              <div class="h-2 overflow-hidden rounded bg-slate-100">
                <div
                  class="h-full bg-indigo-500 transition-all"
                  :style="{ width: pctOfTotalBanners(p.projectId) + '%' }"
                />
              </div>
            </li>
          </ul>
        </template>
      </Card>
    </section>

    <!-- Details: Banners table -->
    <section class="mt-6">
      <Card>
        <template #title>Banners</template>
        <template #body>
          <div class="overflow-x-auto max-h-[400px] overflow-y-auto">
            <table class="min-w-full text-sm">
              <thead>
                <tr class="text-left text-slate-500">
                  <th class="py-2 pr-3">Banner</th>
                  <th class="py-2 pr-3">Campaign</th>
                  <th class="py-2 pr-3">Project</th>
                  <th class="py-2 pr-3">Size</th>
                  <th class="py-2 pr-3 text-right">Views</th>
                  <th class="py-2 pr-3 text-right">Clicks</th>
                  <th class="py-2 pr-3 text-right">Link</th>
                  <th class="py-2 pr-3 text-right">CTR</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="b in banners" :key="b.bannerId" class="border-t">
                  <td class="py-2 pr-3">
                    <a
                      v-if="b.imageUrl"
                      :href="`${imageBaseUrl}${b.imageUrl}` || '#'"
                      target="_blank"
                      class="text-indigo-600 hover:underline"
                      >{{ b.bannerName }}</a
                    >
                    <span v-else>—</span>
                  </td>
                  <td class="py-2 pr-3">
                    {{ campaignIndex[b.campaignId]?.campaignName ?? "—" }}
                  </td>
                  <td class="py-2 pr-3">
                    {{
                      projectNameIndex[
                        campaignIndex[b.campaignId]?.projectId
                      ] ?? "—"
                    }}
                  </td>
                  <td class="py-2 pr-3">
                    {{ b.imageWidth ?? "?" }}×{{ b.imageHeight ?? "?" }}
                  </td>
                  <td class="py-2 pr-3 text-right tabular-nums">
                    {{ b.views?.toLocaleString?.() ?? b.views }}
                  </td>
                  <td class="py-2 pr-3 text-right tabular-nums">
                    {{ b.clicks?.toLocaleString?.() ?? b.clicks }}
                  </td>
                  <td class="py-2 pr-3 text-right tabular-nums">
                    <a
                      v-if="b.link"
                      :href="b.link || '#'"
                      target="_blank"
                      class="text-indigo-600 hover:underline"
                      >Open</a
                    >
                    <span v-else>—</span>
                  </td>
                  <td class="py-2 pr-3 text-right tabular-nums">
                    {{ formatPct(ctr(b)) }}%
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </Card>
    </section>
  </div>
  <div
    v-if="errorMessage"
    class="py-4 my-4 bg-red-50 border border-red-100 rounded-md text-center text-red-600"
  >
    {{ errorMessage }}
  </div>
</template>
