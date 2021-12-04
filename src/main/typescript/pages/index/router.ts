import Vue from "vue";
import VueRouter, { RouteConfig } from "vue-router";

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: "",
    name: "index",
    component: () => import("@/pages/index/views/Index.vue")
  },
  {
    path: "*",
    name: "404",
    component: () => import("@/pages/index/views/404.vue")
  }
];

export const router = new VueRouter({
  base: `${process.env.BASE_URL}/login`,
  mode: "history",
  routes: routes
});
