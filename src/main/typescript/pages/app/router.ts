import Vue from "vue";
import VueRouter, { RouteConfig } from "vue-router";

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: "/",
    name: "home",
    component: () => import("@/pages/app/views/Home.vue")
  },
  {
    path: "/group/:id",
    props: true,
    name: "group",
    component: () => import("@/pages/app/views/Group.vue")
  },
  {
    path: "/group/:id/edit",
    props: true,
    name: "group-edit",
    component: () => import("@/pages/app/views/GroupEdit.vue")
  },
  {
    path: "/list/:id",
    props: true,
    name: "list",
    component: () => import("@/pages/app/views/List.vue")
  },
  {
    path: "/group/:id/edit",
    props: true,
    name: "list-edit",
    component: () => import("@/pages/app/views/ListEdit.vue")
  },
  {
    path: "/task/:id",
    props: true,
    name: "task",
    component: () => import("@/pages/app/views/Task.vue")
  },
  {
    path: "/task/:id/edit",
    props: true,
    name: "task-edit",
    component: () => import("@/pages/app/views/TaskEdit.vue")
  },
  {
    path: "*",
    name: "404",
    component: () => import("@/pages/app/views/404.vue")
  }
];

export const router = new VueRouter({
  // base: process.env.BASE_URL,
  base: "/app",
  mode: "history",
  routes: routes
});
