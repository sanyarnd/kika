import Vue from "vue";
import VueRouter, { RouteConfig } from "vue-router";

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: "/",
    name: "index",
    component: () => import("@/pages/index/views/Index.vue")
  }
];

export const router = new VueRouter({
  base: "/login",
  mode: "history",
  routes: routes
});
