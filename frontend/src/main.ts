import { createApp } from "vue";
import { createPinia } from "pinia";
import { createRouter, createWebHashHistory } from "vue-router";
import App from "./App.vue";
import CreateEvent from "./pages/CreateEventPage.vue";
import HostPage from "./pages/HostPage.vue";
import InvitePage from "./pages/InvitePage.vue";
import HomePage from "./pages/HomePage.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: "/home" },
    { path: "/create", component: CreateEvent },
    { path: "/home", component: HomePage },
    { path: "/host/:eventId", component: HostPage, props: true },
    { path: "/invite/:eventId/:inviteId", component: InvitePage, props: true },
  ],
});

createApp(App).use(createPinia()).use(router).mount("#app");
