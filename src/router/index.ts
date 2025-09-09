import { createRouter, createWebHistory } from "vue-router";
import Login from "@/views/auth/Login.vue";
import Register from "@/views/auth/Register.vue";
import { JWTAuthentication } from "@/services/authentication";
import TotalProjects from "@/views/project/TotalProjects.vue";
import CreateProject from "@/views/project/CreateProject.vue";
import EditProject from "@/views/project/EditProject.vue";
import SingleProject from "@/views/project/SingleProject.vue";
import CreateCampaign from "@/views/campaign/CreateCampaign.vue";
import EditCampaign from "@/views/campaign/EditCampaign.vue";
import Dashboard from "@/views/dashboard/Dashboard.vue";
import ProfileDashboard from "@/views/profile/ProfileDashboard.vue";
import ChangePassword from "@/views/auth/ChangePassword.vue";
import EditProfile from "@/views/profile/EditProfile.vue";
import SystemSettings from "@/views/settings/SystemSettings.vue";
import SingleCampaign from "@/views/campaign/SingleCampaign.vue";
import CreateBanner from "@/views/banner/CreateBanner.vue";
import TotalCampaigns from "@/views/campaign/TotalCampaigns.vue";
import TotalBanners from "@/views/banner/TotalBanners.vue";
import ComingSoon from "@/views/others/ComingSoon.vue";
import SingleBanner from "@/views/banner/SingleBanner.vue";
import TotalActionLogs from "@/views/action-log/TotalActionLogs.vue";
import Documentation from "@/views/documentation/Documentation.vue";

const routes = [
    {
        path: '/',
        name: 'Dashboard',
        component: Dashboard,
         meta: {
            title: 'Dashboard - Keetlo Banner Management',
            description: 'Overview of all projects, campaigns, and banners.',
            keywords: 'dashboard, projects, campaigns, banners',
            ogTitle: 'Dashboard - Keetlo Banner Management',
            ogDescription: 'Overview of all projects, campaigns, and banners.',
            ogImage: '/images/logo.jpg',  
        },
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
         meta: {
            title: 'Login - Keetlo Banner Management',
            description: 'Login to your account to manage your projects and campaigns.',
            ogTitle: 'Login - Keetlo Banner Management',
            ogDescription: 'Login to your account to manage your projects and campaigns.',
        },
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
        {
        path: '/projects',
        name: 'TotalProjects',
        component: TotalProjects,
    },
        {
        path: '/projects/:slug',
        name: 'SingleProject',
        component: SingleProject,
    },
    {
        path: '/projects/create',
        name: 'CreateProject',
        component: CreateProject,
    },
     {
        path: '/projects/edit/:slug',
        name: 'EditProject',
        component: EditProject,
    },
    {
        path: '/campaigns',
        name: 'TotalCampaigns',
        component: TotalCampaigns,
    },
     {
        path: '/campaigns/:slug',
        name: 'SignleCampaign',
        component: SingleCampaign,
    },
       {
        path: '/campaigns/create',
        name: 'CreateCampaign',
        component: CreateCampaign,
    },
     {
        path: '/banners',
        name: 'TotalBanners',
        component: TotalBanners,
    },
         {
        path: '/banners/:slug',
        name: 'SingleBanner',
        component: SingleBanner,
    },
    {
        path: '/banners/create',
        name: 'CreateBanner',
        component: CreateBanner,
    },
    {
        path: '/campaigns/edit/:slug',
        name: 'EditCampaign',
        component: EditCampaign,
    },
    {
        path: '/profile',
        name: 'ProfileDashboard',
        component: ProfileDashboard,
    },
    {
        path: '/profile/change-password',
        name: 'ChangePassword',
        component: ChangePassword,
    },
    {
        path: '/profile/edit',
        name: 'EditProfile',
        component: EditProfile,
    },
    {
        path: '/settings',
        name: 'SystemSettings',
        component: SystemSettings,
    }, 
      {
        path: '/action-logs',
        name: 'TotalActionLogs',
        component: TotalActionLogs,
    },
      {
        path: '/documentation',
        name: 'Documentation',
        component: Documentation,
    },
    {
        path: '/coming-soon',
        name: 'ComingSoon',
        component: ComingSoon,
    },
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
})

router.beforeEach((to, _, next)=>{
   const jwtAuthentication = JWTAuthentication();
      if(!jwtAuthentication && to.path !== '/login' && to.path !== '/register') {
         return next("/login");
      } 
      if(jwtAuthentication && (to.path === '/login' || to.path === '/register')) {
        return next("/");
      }
      next();
});

export default router;