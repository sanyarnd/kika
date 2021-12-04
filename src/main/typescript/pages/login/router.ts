import Vue from "vue";
import VueRouter, { RouteConfig } from "vue-router";

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: "",
    name: "login",
    component: () => import("@/pages/login/views/Login.vue")
  },
  {
    path: "/register",
    name: "register",
    component: () => import("@/pages/login/views/Register.vue")
  },
  {
    path: "*",
    name: "404",
    component: () => import("@/pages/login/views/404.vue")
  }
];

export const router = new VueRouter({
  base: `${process.env.BASE_URL}/login`,
  mode: "history",
  routes: routes
});
